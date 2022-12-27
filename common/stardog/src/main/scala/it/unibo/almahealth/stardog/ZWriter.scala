package it.unibo.almahealth.stardog

import com.complexible.common.base.Options
import com.stardog.stark.Statement
import com.stardog.stark.io.RDFWriter
import com.stardog.stark.io.turtle.TurtleWriter
import zio.ZIO

import java.io.ByteArrayOutputStream
import java.io.OutputStream

type BAOS = ByteArrayOutputStream

def withBAOS[R, E](f: BAOS => ZIO[R, E, Unit]): ZIO[R, E, String] =
  val baos = new ByteArrayOutputStream
  f(baos).map(_ => String(baos.toByteArray()))

trait ZWriter(writerFactory: OutputStream => RDFWriter):
  private def start(baos: BAOS): ZIO[Any, Nothing, RDFWriter] =
    val writer = writerFactory(baos)
    writer.start()
    ZIO.succeed(writer)

  private def end(writer: RDFWriter): ZIO[Any, Nothing, Unit] =
    ZIO.succeed(writer.end())

  def write(as: Iterable[Statement]): ZIO[Any, Nothing, String] =
    withBAOS { baos =>
      ZIO.acquireReleaseWith(start(baos))(end) { writer =>
        ZIO.succeed(as.foreach(writer.handle))
      }
    }

  def write(statement: Statement, rest: Statement*): ZIO[Any, Nothing, String] =
    write(statement +: rest)

class ZTurtleWriter extends ZWriter(TurtleWriter.TurtleWriterFactory().create(_, Options.empty()))
