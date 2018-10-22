package transport.model

//Todo Cycle should be able to have more then 4 connections
case class Cycle(initial: Connection, vertical: Connection, corner: Connection, horizontal: Connection)
