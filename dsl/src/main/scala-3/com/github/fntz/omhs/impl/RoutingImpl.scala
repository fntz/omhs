package com.github.fntz.omhs.impl

import com.github.fntz.omhs.*
import com.github.fntz.omhs.internal.*
import com.github.fntz.omhs.streams.ChunkedOutputStream
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.multipart.FileUpload

import java.util.UUID
import scala.collection.mutable.ListBuffer
import scala.quoted.*

private[omhs] object RoutingImpl {

  def generate[F: Type](rule: Expr[Rule], f: Expr[F])(using Quotes): Expr[ExecutableRule] = {
    import Shared.*
    import quotes.reflect.*

    val logger = new Logger()

    sealed trait ParamToken:
      def isBody = false
      def isFile = false

    case object StringToken extends ParamToken
    case object LongToken extends ParamToken
    case object UUIDToken extends ParamToken
    case object RegexToken extends ParamToken
    case object AlternativeToken extends ParamToken
    case object TailToken extends ParamToken
    case class BodyToken(tpe: TypeRepr) extends ParamToken:
      override def isBody: Boolean = true
    case object HeaderToken extends ParamToken
    case object CurrentRequestToken extends ParamToken
    case object FileToken extends ParamToken:
      override def isFile = true
    case object CookieToken extends ParamToken
    case class QueryToken(tpe: TypeRepr) extends ParamToken
    case object StreamToken extends ParamToken

    def tokenType(p: ParamToken): TypeRepr = p match
      case StringToken           => TypeRepr.of[String]
      case LongToken             => TypeRepr.of[Long]
      case UUIDToken             => TypeRepr.of[UUID]
      case RegexToken            => TypeRepr.of[String]
      case TailToken             => TypeRepr.of[List[String]]
      case AlternativeToken      => TypeRepr.of[String]
      case BodyToken(tpt)        => tpt
      case HeaderToken           => TypeRepr.of[String]
      case CurrentRequestToken   => TypeRepr.of[CurrentHttpRequest]
      case FileToken             => TypeRepr.of[List[FileUpload]]
      case CookieToken           => TypeRepr.of[Cookie]
      case QueryToken(tpt)       => tpt
      case StreamToken           => TypeRepr.of[ChunkedOutputStream]

    val availableParams = Map(
      "string" -> StringToken,
      "long" -> LongToken,
      "uuid" -> UUIDToken,
      "regex" -> RegexToken,
      "header" -> HeaderToken,
      "cookie" -> CookieToken,
      "file" -> FileToken,
      "*" -> TailToken,
      "$times" -> TailToken
    )

    def unwrap(tree: Tree): Tree = tree match
      case Inlined(_, _, t) => unwrap(t)
      case Block(stats, expr) =>
        unwrap(expr) match
          case Closure(meth, _) =>
            stats.collectFirst {
              case d: DefDef if d.symbol == meth.symbol => d
            }.getOrElse(unwrap(expr))
          case other => other
      case Closure(meth, _) =>
        meth.symbol.tree match
          case d: DefDef => d
          case _         => tree
      case other => other

    val tokens = ListBuffer.empty[ParamToken]

    def addParamToken(name: String): Unit =
      availableParams.get(name).foreach(tokens += _)

    def isStringExt(term: Term): Boolean =
      // Use the term's result type — Apply(StringExt.|(...)).symbol is the method itself.
      val n = term.tpe.widen.typeSymbol.name
      n.contains("StringExt")

    object TokenTraverser extends TreeTraverser:
      override def traverseTree(tree: Tree)(owner: Symbol): Unit =
        tree match
          // StringExt("a").|("b")
          case Select(qual, "|") if isStringExt(qual) =>
            tokens += AlternativeToken
            traverseTreeChildren(tree)(owner)
          case Apply(TypeApply(fun, List(tpt)), args) =>
            fun.symbol.name match
              case "body"  => tokens += BodyToken(tpt.tpe)
              case "query" => tokens += QueryToken(tpt.tpe)
              case _       => ()
            // do not re-visit `fun` (would double-count), only args / targs
            args.foreach(a => traverseTree(a)(owner))
          case Apply(fun, args) if availableParams.contains(fun.symbol.name) =>
            // header("x"), regex(...), ... — skip `fun` to avoid Ident double-count
            addParamToken(fun.symbol.name)
            args.foreach(a => traverseTree(a)(owner))
          // imported: Ident("long"); qualified: Select(RoutingDSL, "long")
          case Ident(name) if availableParams.contains(name) =>
            addParamToken(name)
          case Select(_, name) if availableParams.contains(name) =>
            addParamToken(name)
            traverseTreeChildren(tree)(owner)
          case _ =>
            traverseTreeChildren(tree)(owner)

    TokenTraverser.traverseTree(rule.asTerm)(Symbol.spliceOwner)

    val fTerm = unwrap(f.asTerm)
    guardRoutes(f.asTerm)

    logger.info(s"Found ${tokens.size} arguments for rule")

    val actualFunctionParameters: Seq[(TypeRepr, String)] = fTerm match
      case Lambda(params, _) =>
        params.map(p => (p.tpt.tpe, p.name))
      case d: DefDef =>
        d.termParamss.flatMap(_.params).map(p => (p.tpt.tpe, p.name))
      case _ =>
        Nil

    val isEmptyFunction = actualFunctionParameters.isEmpty

    val reqType = TypeRepr.of[CurrentHttpRequest]
    def isReqParam(x: TypeRepr): Boolean = x.widen.dealias =:= reqType
    val isReqParamNeeded = actualFunctionParameters.exists((t, _) => isReqParam(t))

    val chunkedStreamType = TypeRepr.of[ChunkedOutputStream]
    def isStreamParam(x: TypeRepr): Boolean = x.widen.dealias =:= chunkedStreamType
    val isStreamNeeded = actualFunctionParameters.exists((t, _) => isStreamParam(t))

    logger.info(
      s"""
         |isEmptyFunction? $isEmptyFunction
         |need to pass requestParam? $isReqParamNeeded
         |need to pass stream? $isStreamNeeded
         |detected tokens: ${tokens.mkString(", ")}
         |actual parameters: ${actualFunctionParameters.map(x => s"${x._2}: ${x._1.show}").mkString(", ")}
         |""".stripMargin
    )

    if isReqParamNeeded && isStreamNeeded then
      val size = actualFunctionParameters.size
      val isReqLastOrPenultimate = isReqParam(actualFunctionParameters.last._1) ||
        (size > 1 && isReqParam(actualFunctionParameters(size - 2)._1))
      val isStreamLastOrPenultimate = isStreamParam(actualFunctionParameters.last._1) ||
        (size > 1 && isStreamParam(actualFunctionParameters(size - 2)._1))
      if !(isReqLastOrPenultimate && isStreamLastOrPenultimate) then
        report.errorAndAbort(
          s"${reqType.show} or ${chunkedStreamType.show} must be the last arguments in the function"
        )
      else if isReqParam(actualFunctionParameters.last._1) then
        tokens += StreamToken
        tokens += CurrentRequestToken
      else
        tokens += CurrentRequestToken
        tokens += StreamToken
    else
      if isReqParamNeeded && !isReqParam(actualFunctionParameters.last._1) then
        report.error(s"${reqType.show} must be the last argument in the function")
      else if isReqParamNeeded then
        tokens += CurrentRequestToken
      if isStreamNeeded && !isStreamParam(actualFunctionParameters.last._1) then
        report.error(s"${chunkedStreamType.show} must be the last argument in the function")
      else if isStreamNeeded then
        tokens += StreamToken

    if tokens.size != actualFunctionParameters.size && !isEmptyFunction then
      report.error(
        s"Args lengths are not the same. " +
          s"function has ${actualFunctionParameters.size} params, route definition parsed to ${tokens.size}"
      )

    if !isEmptyFunction then
      tokens.zip(actualFunctionParameters).foreach { case (paramToken, (funcTypeParam, argName)) =>
        val at = tokenType(paramToken)
        if !(at.widen.dealias =:= funcTypeParam.widen.dealias) then
          report.errorAndAbort(
            s"Incorrect type for `$argName`, " +
              s"required: ${at.typeSymbol.name}, given: ${funcTypeParam.show}"
          )
      }

    val bodyCount = tokens.count(_.isBody)
    if bodyCount > 1 then
      report.errorAndAbort(s"BodyParam must be one per rule, given: $bodyCount")
    val fileCount = tokens.count(_.isFile)
    if fileCount > 1 then
      report.errorAndAbort(s"FileParam must be one per rule, given: $fileCount")
    if fileCount == 1 && bodyCount == 1 then
      report.errorAndAbort("You can not mix BodyParam with FileParam, choose one")

    val defsPositions: List[Int] = tokens.map {
      case StringToken         => ParamDef.string
      case LongToken           => ParamDef.long
      case RegexToken          => ParamDef.regex
      case UUIDToken           => ParamDef.uuid
      case TailToken           => ParamDef.tail
      case AlternativeToken    => ParamDef.alternative
      case BodyToken(_)        => ParamDef.body
      case HeaderToken         => ParamDef.header
      case CurrentRequestToken => ParamDef.request
      case FileToken           => ParamDef.file
      case CookieToken         => ParamDef.cookie
      case QueryToken(_)       => ParamDef.query
      case StreamToken         => ParamDef.stream
    }.toList

    val positionsExpr = Expr.ofList(defsPositions.map(Expr(_)))

    def extractAt(sorted: Expr[List[ParamDef[?]]], idx: Int, token: ParamToken): Expr[Any] =
      val i = Expr(idx)
      token match
        case StringToken =>
          '{ $sorted($i).asInstanceOf[StringDef].value }
        case LongToken =>
          '{ $sorted($i).asInstanceOf[LongDef].value }
        case RegexToken =>
          '{ $sorted($i).asInstanceOf[RegexDef].value }
        case UUIDToken =>
          '{ $sorted($i).asInstanceOf[UUIDDef].value }
        case TailToken =>
          '{ $sorted($i).asInstanceOf[TailDef].value }
        case AlternativeToken =>
          '{ $sorted($i).asInstanceOf[AlternativeDef].value }
        case BodyToken(tpt) =>
          tpt.asType match
            case '[t] => '{ $sorted($i).asInstanceOf[BodyDef[t]].value }
        case HeaderToken =>
          '{ $sorted($i).asInstanceOf[HeaderDef].value }
        case CurrentRequestToken =>
          '{ $sorted($i).asInstanceOf[CurrentHttpRequestDef].value }
        case FileToken =>
          '{ $sorted($i).asInstanceOf[FileDef].value }
        case CookieToken =>
          '{ $sorted($i).asInstanceOf[CookieDef].value }
        case QueryToken(tpt) =>
          tpt.asType match
            case '[t] => '{ $sorted($i).asInstanceOf[QueryDef[t]].value }
        case StreamToken =>
          '{ $sorted($i).asInstanceOf[StreamDef].value }

    def returnTypeOfF: TypeRepr =
      TypeRepr.of[F] match
        case AppliedType(_, args) if args.nonEmpty => args.last
        case _ =>
          fTerm match
            case Lambda(_, body) => body.tpe.widen
            case d: DefDef       => d.returnTpt.tpe
            case _               => TypeRepr.of[Any]

    val resultExpr: Expr[ExecutableRule] = returnTypeOfF.asType match
      case '[r] =>
        val conv = Expr.summon[r => AsyncResult].getOrElse {
          report.errorAndAbort(
            "Cannot convert route result to AsyncResult. Import com.github.fntz.omhs.AsyncResult.Implicits._"
          )
        }

        '{
          val rule0 = $rule
          if ${ Expr(isReqParamNeeded) } then { val _ = rule0.withRequest() }
          if ${ Expr(isStreamNeeded) } then { val _ = rule0.withStream() }

          new ExecutableRule(
            rule0,
            (defs: List[ParamDef[?]]) => {
              val defsMap = defs.groupBy(_.sortProp).map { x =>
                x._1 -> x._2.toBuffer[ParamDef[?]]
              }
              val sorted = $positionsExpr.map(x => defsMap(x).remove(0))
              val raw: r = ${
                if isEmptyFunction then
                  Apply(Select.unique(f.asTerm, "apply"), Nil).asExprOf[r]
                else
                  val args = tokens.zipWithIndex.map { case (token, idx) =>
                    extractAt('sorted, idx, token).asTerm
                  }.toList
                  Apply(Select.unique(f.asTerm, "apply"), args).asExprOf[r]
              }
              $conv(raw)
            }
          )
        }

    logger.verbose(resultExpr.asTerm.show)
    resultExpr
  }
}
