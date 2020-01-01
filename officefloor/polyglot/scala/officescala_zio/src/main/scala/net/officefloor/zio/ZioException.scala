package net.officefloor.zio

/**
 * ZIO {@link Exception}.
 */
class ZioException(val message: String, val zioCause: Any) extends RuntimeException(message)
