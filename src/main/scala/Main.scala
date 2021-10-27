package keyedsemaphore

import cats.Hash
import cats.effect.{Concurrent, IO}
import cats.effect.std.Semaphore
import cats.effect.{IO, Sync, Ref}
import cats.syntax.all._
import cats.implicits._

final case class KeyedSemaphore(
  permitsPerKey: Int,
  private val state: Ref[IO, Map[Int, Semaphore[IO]]],
)(implicit C: Concurrent[IO]) {

  private def ensureSemaphoreExists(hashKey: Int): IO[Unit] =
    for {
      kvs       <- state.get
      semaphore <- kvs.get(hashKey).fold(Semaphore[IO](permitsPerKey))(IO.pure)
      _         <- state.update(_ + (hashKey -> semaphore))
    } yield ()

  def withPermit[K: Hash, A](key: K, action: IO[A]): IO[A] =
    for {
      _   <- ensureSemaphoreExists(key.hash)
      kvs <- state.get
      res <- kvs(key.hash).permit.surround(action)
      _   <- state.set(kvs.removed(key.hash))
    } yield res
}

object KeyedSemaphore {

  def apply(permitsPerKey: Int)(implicit C: Concurrent[IO]): IO[KeyedSemaphore] =
    Ref.of[IO, Map[Int, Semaphore[IO]]](Map.empty).map(KeyedSemaphore(permitsPerKey, _))
}
