package net.officefloor.tutorial.catshttpserver

import cats.effect.IO
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import doobie.util.transactor.Transactor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

import javax.sql.DataSource
import scala.concurrent.ExecutionContext

// START SNIPPET: tutorial
@SpringBootApplication
class Application {

  @Bean
  def scalaJacksonModule(): Module = DefaultScalaModule

  @Bean
  def transactor(dataSource: DataSource): Transactor[IO] =
    Transactor.fromDataSource[IO](dataSource, ExecutionContext.global)
}

object Application extends App {
  SpringApplication.run(classOf[Application], args: _*)
}
// END SNIPPET: tutorial
