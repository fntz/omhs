package com.github.fntz.omhs.internal

import com.github.fntz.omhs.Rule

// rule syntax
sealed trait LikeRule {
  val rule = new Rule

  override def toString: String = rule.toString
}

trait FileOrBodyLikeParam extends LikeRule
object FileOrBodyLikeParam {
  def apply(r: Rule): FileOrBodyLikeParam = new FileOrBodyLikeParam {
    override val rule: Rule = r
  }
}

trait HeaderOrCookieLikeParam extends LikeRule {

  def <<(h: HeaderParam): HeaderOrCookieLikeParam = {
    val tmp = rule.header(h)
    new HeaderOrCookieLikeParam {
      override  val rule: Rule = tmp
    }
  }

  def <<<(c: CookieParam): HeaderOrCookieLikeParam = {
    val tmp = rule.cookie(c)
    new HeaderOrCookieLikeParam {
      override  val rule: Rule = tmp
    }
  }

  def <<<[T](bodyParam: BodyParam[T]): FileOrBodyLikeParam = {
    FileOrBodyLikeParam(rule.same.body(bodyParam.reader))
  }

  def <<<(fileParam: FileParam): FileOrBodyLikeParam = {
    FileOrBodyLikeParam(rule.same.withFiles(fileParam))
  }
}

object HeaderOrCookieLikeParam {
  def apply(r: Rule): HeaderOrCookieLikeParam = new HeaderOrCookieLikeParam {
    override val rule: Rule = r
  }
}

trait QueryLikeParam extends HeaderOrCookieLikeParam {
  def :?[T](implicit qr: QueryParam[T]): QueryLikeParam = {
    val tmp = rule.query(qr.reader)
    new QueryLikeParam {
      override  val rule: Rule = tmp
    }
  }
}
object QueryLikeParam {
  def apply(r: Rule): QueryLikeParam = new QueryLikeParam {
    override val rule: Rule = r
  }
}

// after tail
trait NoPathMoreParam extends QueryLikeParam with HeaderOrCookieLikeParam
object NoPathMoreParam {
  def apply(r: Rule): NoPathMoreParam = new NoPathMoreParam {
    override val rule: Rule = r
  }
}

// val r = "asd" | "qwe" / uuid
// val r = uuid / "asd" | "qwe"

trait PathLikeParam extends QueryLikeParam with HeaderOrCookieLikeParam {

  def |(str: String): PathLikeParam = {
    val copied = rule.same
    copied.currentParams.lastOption match {
      case Some(AlternativeParam(vs)) =>
        val xs = copied.currentParams
        val tmp = rule.same(clearParams = true)
        copied.currentParams.slice(0, xs.length - 1).foreach { p =>
          tmp.path(p)
        }
        PathLikeParam(tmp.path(AlternativeParam(vs :+ str)))
      case _ =>
        PathLikeParam(copied.path(AlternativeParam(str :: Nil)))
    }
  }

  def /(str: String): PathLikeParam = {
    /(HardCodedParam(str))
  }

  def /(p: TailParam.type): NoPathMoreParam = {
    NoPathMoreParam(rule.same.path(p))
  }

  def /(p: PathParam): PathLikeParam = {
    PathLikeParam(rule.same.path(p))
  }

}
object PathLikeParam {
  def apply(r: Rule): PathLikeParam = new PathLikeParam {
    override val rule: Rule = r
  }
}