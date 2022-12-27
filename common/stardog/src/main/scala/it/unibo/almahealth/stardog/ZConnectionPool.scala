package it.unibo.almahealth.stardog

import com.complexible.stardog.api.ConnectionPool
import zio.ZIO
import com.complexible.stardog.api.Connection

class ZConnectionPool(connectionPool: ConnectionPool):

  def obtain: ZIO[Any, Nothing, Connection] = ZIO.succeed(connectionPool.obtain())

  def release(connection: Connection): ZIO[Any, Nothing, Unit] =
    ZIO.succeed(connectionPool.release(connection))

  def withConnection[R, E, A](f: ZConnection => ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.acquireReleaseWith(obtain)(release)(f compose ZConnection.apply)
