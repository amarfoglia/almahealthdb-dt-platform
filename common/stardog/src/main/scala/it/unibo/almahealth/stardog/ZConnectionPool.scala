package it.unibo.almahealth.stardog

import com.complexible.stardog.api.Connection
import com.complexible.stardog.api.ConnectionConfiguration
import com.complexible.stardog.api.ConnectionPool
import com.complexible.stardog.api.ConnectionPoolConfig
import zio.ZIO
import zio.ZLayer

class ZConnectionPool(connectionPool: ConnectionPool):

  private def obtain: ZIO[Any, Nothing, Connection] = ZIO.succeed(connectionPool.obtain())

  private def release(connection: Connection): ZIO[Any, Nothing, Unit] =
    ZIO.succeed(connectionPool.release(connection))

  def withConnection[R, E, A](f: ZConnection => ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.acquireReleaseWith(obtain)(release)(f compose ZConnection.apply)

object ZConnectionPool:
  def live(
      connectionConfiguration: ConnectionConfiguration
  ): ZLayer[Any, Throwable, ZConnectionPool] =
    ZLayer {
      ZIO
        .attempt {
          ConnectionPoolConfig.using(connectionConfiguration).create()
        }
        .map(ZConnectionPool(_))
    }
