package net.officefloor.tutorial.catshttpserver

import cats.effect.IO
import doobie.util.log.{ExecFailure, LogEvent, LogHandler, ProcessingFailure, Success}

import java.util.logging.Logger

class JavaUtilLogHandler(logger: Logger) extends LogHandler[IO] {

  def run(logEvent: LogEvent): IO[Unit] = {
    IO {
      logEvent match {
        case success: Success => this.logger.fine(success.sql)
        case failure: ProcessingFailure => this.logger.warning(failure.failure.toString)
        case failure: ExecFailure => this.logger.warning(failure.failure.toString)
      }
    }
  }

}
