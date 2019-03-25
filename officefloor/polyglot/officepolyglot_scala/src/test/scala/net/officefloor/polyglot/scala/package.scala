package net.officefloor.polyglot

package object scala {

  def unifiedTypes(_byte: Byte, _short: Short, _char: Char, _int: Int, _long: Long, _float: Float, _double: Double): UnifiedTypes = {
    new UnifiedTypes(_byte, _short, _char, _int, _long, _float, _double)
  }

  def objects(_java: JavaObject): JavaObject = _java

  def collections(_list: java.util.List[Int], _set: java.util.Set[Long], _map: java.util.Map[String, Char], _collection: java.util.Collection[Byte]): CollectionTypes = {
    new CollectionTypes(_list, _set, _map, _collection)
  }

}