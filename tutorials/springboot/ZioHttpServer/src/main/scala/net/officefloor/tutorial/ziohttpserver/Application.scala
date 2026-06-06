package net.officefloor.tutorial.ziohttpserver

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

// START SNIPPET: tutorial
@SpringBootApplication
class Application {

  @Bean
  def scalaJacksonModule(): Module = DefaultScalaModule
}

object Application extends App {
  SpringApplication.run(classOf[Application], args: _*)
}
// END SNIPPET: tutorial
