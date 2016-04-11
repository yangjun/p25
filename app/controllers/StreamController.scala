package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl._
import play.api.mvc.{Action, Controller}
import play.libs.Json

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Created by 军 on 2016/3/29.
 */
@Singleton
class StreamController @Inject()(implicit val mat: Materializer, implicit val exec: ExecutionContext) extends Controller {

  def m = Action.async {
    //    val values = 1 to 100
    //    val source = Source.fromIterator(() => values.iterator)
    val source = Source(1L to 100000L)
    val sink = Sink.fold[Long, Long](0L)(_ + _)
    val runnableGraph = source.toMat(sink)(Keep.right)
    //    val result = source.runWith(sink)
    val result = runnableGraph.run()
    result.map(f => Ok("" + f))
    //    RunnableGraph.fromGraph(GraphDSL.create() {
    //      implicit builder =>
    //        val a = builder.add(source).out
    //
    //        ClosedShape
    //    })
    //    Ok("111")
  }

  private def prefixAndAuthor = {
    ("1", "2")
  }

  def m1 = Action {
    val source = Source.tick(initialDelay = 0 second, interval = 1 second, tick = "tick")
    Ok.chunked(source.map { tick => {
      val (prefix, author) = prefixAndAuthor
      Json.toJson("message" -> s"$prefix", "author" -> s"$author").toString + "\n"
    }
    }.limit(100))
  }

}
