package com.magicube.eventflows.web.Controllers

import org.springframework.web.bind.annotation.{RequestMapping, RestController}

@RestController
class HomeController {
  @RequestMapping(Array[String]("/"))
  def index(): String = "home"
}
