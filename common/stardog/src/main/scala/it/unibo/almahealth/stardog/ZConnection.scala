package it.unibo.almahealth.stardog

import com.complexible.stardog.StardogException
import com.complexible.stardog.api.Connection
import com.complexible.stardog.api.ConnectionConfiguration
import com.complexible.stardog.api.ConnectionPool
import com.complexible.stardog.api.Query
import com.stardog.stark.Statement
import com.stardog.stark.query.BindingSet
import com.stardog.stark.query.SelectQueryResult
import com.stardog.stark.query.io.QueryResultWriters
import zio.ZIO
import zio.stream.ZStream

import scala.jdk.StreamConverters._

import util.chaining.*
import ZConnection.*

class ZConnection(
    private val connection: Connection,
    private val namespaces: List[Namespace]
):

  def addNamespace(namespace: Namespace): ZConnection =
    new ZConnection(connection, namespace :: namespaces)

  def select(
      in: String,
      parameters: List[Parameter] = List()
  ): ZStream[Any, StardogException, BindingSet] =
    ZStream
      .fromJavaStream(
        connection
          .tap(ensureNamespaces)
          .select(in)
          .tap(applyParameters(parameters))
          .execute()
          .stream()
      )
      .refineToOrDie[StardogException]

  def graph(
      in: String,
      parameters: List[Parameter] = List()
  ): ZStream[Any, StardogException, Statement] =
    ZStream
      .fromJavaStream(
        connection
          .tap(ensureNamespaces)
          .graph(in)
          .tap(applyParameters(parameters))
          .execute()
          .stream()
      )
      .refineToOrDie[StardogException]

  private def ensureNamespaces(connection: Connection): Unit =
    def go(namespace: Namespace): Unit =
      if !connection.namespaces.iri(namespace.base).isPresent then
        connection.namespaces.add(namespace.base, namespace.uri)
    namespaces.foreach(go)

  private def applyParameters[A](parameters: List[Parameter])(query: Query[A]): Unit =
    parameters.foreach { case Parameter(variable, value) =>
      query.parameter(variable, value)
    }

object ZConnection:

  case class Namespace(base: String, uri: String)

  case class Parameter(variable: String, value: String)

  def apply(connection: Connection) =
    new ZConnection(connection, List())
