package com.github.fntz.omhs.util

import com.github.fntz.omhs.Rule
import org.slf4j.LoggerFactory

import scala.annotation.tailrec


object OverlapDetector {

  private val logger = LoggerFactory.getLogger(getClass)

  private case class TheSame(url1: String, url2: String)

  def detect(rules: Vector[Rule]): Unit = {
    rules.groupBy(_.currentMethod).flatMap { case (_, xs) =>
      xs.combinations(2).flatMap {
        case Vector(r1, r2) =>
          if (isOverlapping(r1, r2)) {
            Some(TheSame(r1.currentUrl, r2.currentUrl))
          } else {
            None
          }

        case _ =>
          None
      }
    }.foreach { xs =>
      logger.warn(s"Overlap is detected: ${xs.url1} and ${xs.url2}")
    }
  }

  def isOverlapping(r1: Rule, r2: Rule): Boolean = {
    val ps1 = r1.currentParams.toList
    val ps2 = r2.currentParams.toList

    if (r1.currentUrl == r2.currentUrl) {
      true
    } else if (r1.hasTail || r2.hasTail) {
      @tailrec
      def rec(index: Int, isDone: Boolean): Boolean = {
        if (isDone) {
          false
        } else {
          (ps1.lift(index), ps2.lift(index)) match {
            case (Some(p1), Some(_)) if p1.isRestParam =>
              true
            case (Some(_), Some(p2)) if p2.isRestParam =>
              true
            case (Some(_), Some(_)) =>
              rec(index + 1, isDone = false)
            case _ =>
              false
          }
        }
      }
      rec(0, isDone = false)
    } else {
      false
    }

  }

}














