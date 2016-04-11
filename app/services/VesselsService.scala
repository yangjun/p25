package services

import java.util.UUID
import javax.inject.Inject

import models.Vessel
import repository.{VesselsMongoRepository, VesselsRepository}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by 军 on 2016/4/9.
 */

class VesselsService  @Inject() (val vesselsRepository: VesselsMongoRepository) {
    def create(vessel: Vessel)(implicit ec: ExecutionContext): Future[Either[String, UUID]] =  {
      vesselsRepository.create(vessel)
    }
}
