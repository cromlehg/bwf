package models.dao

import javax.inject.{Inject, Singleton}

trait DAOProvider {

	val accounts: AccountDAO
	val chatMsgs: ChatMsgDAO
	val comments: CommentDAO
	val menu: MenuDAO
	val options: OptionDAO
	val permissions: PermissionDAO
	val posts: PostDAO
	val roles: RoleDAO
	val sessions: SessionDAO

}

@Singleton
class SlickDAOProvider @Inject()(
	override val accounts: AccountDAO,
	override val chatMsgs: ChatMsgDAO,
	override val comments: CommentDAO,
	override val menu: MenuDAO,
	override val options: OptionDAO,
	override val permissions: PermissionDAO,
	override val posts: PostDAO,
	override val roles: RoleDAO,
	override val sessions: SessionDAO,
) extends DAOProvider {

}
