package com.magicube.eventflows

trait Awaiting {

  import scala.concurrent._
  import scala.concurrent.duration._

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val timeout: FiniteDuration = 5.seconds

  def await[A](future: Future[A])(implicit timeout: Duration = Duration.Inf): A = Await.result(future, timeout)
}
