package models.dao.slick

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.ExecutionContext

class SlickEitherT[A, B, E <: Effect](val value: DBIOAction[Either[A, B], NoStream, E]) {

	def map[C](f: B => C)(implicit
												ec: ExecutionContext): SlickEitherT[A, C, E] = new SlickEitherT(value.map({
		case Right(b) => Right(f(b))
		case Left(a) => Left(a)
	}))

	def flatMap[C, F <: Effect](f: B => SlickEitherT[A, C, F])(implicit
																														 ec: ExecutionContext): SlickEitherT[A, C, E with F] = {
		new SlickEitherT(value.flatMap {
			case Right(b) => f(b).value
			case Left(a) => DBIOAction.successful(Left(a))
		})
	}

	def flatMapF[C, F <: Effect](f: B => DBIOAction[Either[A, C], NoStream, F])(implicit
																																							ec: ExecutionContext): SlickEitherT[A, C, E with F] = {
		new SlickEitherT(value.flatMap {
			case Right(b) => f(b)
			case Left(a) => DBIOAction.successful(Left(a))
		})
	}

	def ensure(onFailure: => A)(predicate: B => Boolean)(implicit
																											 ec: ExecutionContext): SlickEitherT[A, B, E] = {
		new SlickEitherT(value.map {
			case Right(b) if predicate(b) => Right(b)
			case _ => Left(onFailure)
		})
	}

	def assure(onFailure: B => A)(predicate: B => Boolean)(implicit
																												 ec: ExecutionContext): SlickEitherT[A, B, E] = {
		new SlickEitherT(value.map {
			case Right(b) if predicate(b) => Right(b)
			case Right(b) => Left(onFailure(b))
			case Left(a) => Left(a)
		})
	}

	def leftMap[D](f: A => D)(implicit
														ec: ExecutionContext): SlickEitherT[D, B, E] = new SlickEitherT(value.map {
		case Right(b) => Right(b)
		case Left(a) => Left(f(a))
	})

}

object SlickEitherT {
	def apply[A, B, E <: Effect](dBIOAction: DBIOAction[Either[A, B], NoStream, E]) = new SlickEitherT(dBIOAction)
}
