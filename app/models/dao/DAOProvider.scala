package models.dao

import javax.inject.{Inject, Singleton}

trait DAOProvider {

	val accounts: AccountDAO
	val options: OptionDAO
	val permissions: PermissionDAO
	val roles: RoleDAO
	val sessions: SessionDAO
	val platformUsers: PlatformUserDAO

}

@Singleton
class SlickDAOProvider @Inject()(override val accounts: AccountDAO,
																 override val options: OptionDAO,
																 override val permissions: PermissionDAO,
																 override val roles: RoleDAO,
																 override val sessions: SessionDAO,
																 override val platformUsers: PlatformUserDAO
																) extends DAOProvider {

}
