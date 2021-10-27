import java.io.File
import scala.concurrent.ExecutionContext
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import keyedsemaphore.KeyedSemaphore

import org.scalatest.flatspec.AnyFlatSpec

class ConcurrencyUtilsSpec extends AnyFlatSpec {

  behavior of "the Keyed Semaphore"

  def createOrThrow(file: File): IO[Unit] =
    if (file.exists) IO.raiseError(new Error("File exists"))
    else IO(file.createNewFile()).void

  it should "not throw during parallelisable workloads" in {

    val parallelisable =
      for {
        bop <- KeyedSemaphore(1)
        _   <- (0 to 100)
                .toList
                .parTraverse(nb => {
                  val file = new File(s"/tmp/$nb")
                  bop.withPermit(nb.toString, createOrThrow(file) >> IO(file.delete()))
                })
      } yield ()
    parallelisable.unsafeRunSync()
  }

  it should "not throw during linear workloads" in {

    val linear =
      for {
        bop <- KeyedSemaphore(1)
        _   <- (0 to 100)
                .toList
                .parTraverse(nb => {
                  val file = new File(s"/tmp/$nb")
                  bop.withPermit((), createOrThrow(file) >> IO(file.delete()))
                })
      } yield ()
    linear.unsafeRunSync()
  }
}
