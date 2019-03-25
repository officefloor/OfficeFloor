package net.officefloor.polyglot.scala

class CollectionTypes(_list: java.util.List[Int], _set: java.util.Set[Long], _map: java.util.Map[String, Char], _collection: java.util.Collection[Byte]) {
  def getList() = _list
  def getSet() = _set
  def getMap() = _map
  def getCollection() = _collection
}