package controllers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers
import directives.VerifyToken
import models.Book
import services.{Job, TokenService}
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
//import io.swagger.annotations._
//import javax.ws.rs.Path
//import service.SearchService


import services.JobService
import akka.http.scaladsl.model.StatusCodes
import scala.io.Source

trait JobJson extends SprayJsonSupport with DefaultJsonProtocol {
  import services.FormatService._

  // CHANGE2: use `jsonFormat7` instead of `jsonFormat6`
  implicit val jobFormat = jsonFormat3(Job)

}


class JobController( val tokenService: TokenService)(implicit val ec: ExecutionContext)extends Directives with JobJson   with PredefinedFromStringUnmarshallers
  with VerifyToken {

  //implicit val jobFormat = jsonFormat3(Job)

  def getJobRoute = path("getJob" / IntNumber) { (jobId) =>
    get {
      verifyToken { user =>
        //log.info("entering " + query)
        //validate(!query.trim.isEmpty ,SearchRoutes.EMPTY_QRY) {

        onComplete(JobService.findJob(jobId)) {
          case Success(res) => res match {
            case Some(job) => complete(job)
            case None => complete(NotFound, s"No such job")
          }
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }

      }

    }

  }

  def startJob = path("startJob" / Segment) { (jobName) =>
    get {

      //log.info("entering " + query)
      //validate(!query.trim.isEmpty ,SearchRoutes.EMPTY_QRY) {

      onComplete(JobService.startJob(jobName)) {
        case Success(res) => res match {
          case Some(job) => complete(job)
          case None      => complete(NotFound, s"Job Couldnt be started")
        }
        case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
      }
      //}
    }

  }

  def upload = path("upload") {
    uploadedFile("csv") {
      case (metadata, file) =>
        print ( file.getName )
        val source = Source.fromFile(file)
        for (line <- source.getLines())
          println(line)
        source.close
        // do something with the file and file metadata ...
        file.delete()
        complete(StatusCodes.OK)
    }
  }

  val routes: Route = startJob ~ getJobRoute ~ upload
}
