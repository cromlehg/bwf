package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.TagTargetTypes.TagTargetTypes
import models.dao.TagDAO
import models.dao.slick.table.TagTable
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickTagDAO @Inject()(
                             val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends TagDAO with TagTable with SlickCommontDAO {

  import dbConfig.profile.api._

  import scala.concurrent.Future.{successful => future}

  private val queryTagById = Compiled(
    (id: Rep[Long]) => tableTag.filter(_.id === id))

  def _findTagById(id: Long) =
    queryTagById(id).result.headOption

  def _findTagsByNames(tagNames: Seq[String]) =
    tableTag.filter(_.name.trim.toLowerCase inSet tagNames.map(_.trim.toLowerCase))

  def _findAssigns(tagIds: Seq[Long], targetId: Long, targetType: TagTargetTypes) =
    tableTagToTarget
      .filter(_.targetId === targetId)
      .filter(_.targetType === targetType)
      .filter(_.tagId inSet tagIds)

  def _createTagsIfNotExistsByName(tagNames: Seq[String]) =
    _findTagsByNames(tagNames).result.flatMap { existsTags =>
      if (existsTags.length == tagNames.length)
        DBIO.successful(0)
      else
        (tableTag ++= tagNames
          .filterNot(t => existsTags.map(_.name).contains(t))
          .map(t => models.Tag(0, t.trim.toLowerCase, None)))
          .map(_.getOrElse(0))
    }

	def _findTagsByTargetIdsWithAssigns(targetIds: Seq[Long], targetType: TagTargetTypes) =
		tableTagToTarget
			.filter(_.targetId inSet targetIds)
			.filter(_.targetType === targetType)
			.join(tableTag).on(_.tagId === _.id)

  def _createAssignsIfNotExists(tagIds: Seq[Long], targetId: Long, targetType: TagTargetTypes) =
    _findAssigns(tagIds, targetId, targetType).result.flatMap { existsAssigns =>
      if (existsAssigns.length == tagIds.length)
        DBIO.successful(0)
      else
        (tableTagToTarget ++= tagIds
          .filterNot(t => existsAssigns.map(_._1).contains(t))
          .map(t => (t, targetType, targetId)))
          .map(_.getOrElse(0))
    }

  def _createTagsIfNotExistsByNameAndReturnAll(tagNames: Seq[String]) =
    _createTagsIfNotExistsByName(tagNames) flatMap (_ => _findTagsByNames(tagNames).result)

  def _createTagsIfNotExistsAndAssignToTargetIfNotAssigned(tagNames: Seq[String], targetId: Long, targetType: TagTargetTypes) =
    _createTagsIfNotExistsByNameAndReturnAll(tagNames) flatMap { tags =>
      _createAssignsIfNotExists(tags.map(_.id), targetId, targetType).map(_ => tags)
    }

  def deleteTagAssignsIfExistsAndNotInSet(tagNames: Seq[String], targetId: Long, targetType: TagTargetTypes) =
    tableTagToTarget
      .filter(_.targetId === targetId)
      .filter(_.targetType === targetType)
      .filter(_.tagId in tableTag.filterNot(_.name inSet tagNames).map(_.id))
    .delete

  def _refreshTags(tagNames: Seq[String], targetId: Long, targetType: TagTargetTypes) =
    deleteTagAssignsIfExistsAndNotInSet(tagNames, targetId, targetType) flatMap { _ =>
      _createTagsIfNotExistsByNameAndReturnAll(tagNames) flatMap { tags =>
        _createAssignsIfNotExists(tags.map(_.id), targetId, targetType).map(_ => tags)
      }
    }

  def _findTagsByTargetId(targetId: Long, targetType: TagTargetTypes) =
    tableTag
      .filter(_.id in tableTagToTarget
        .filter(_.targetId === targetId)
        .filter(_.targetType === targetType)
        .map(_.tagId))

  def _createTag(name: String, descr: Option[String]) =
    (tableTag returning tableTag.map(_.id) into ((v, id) => v.copy(id = id))) += models.Tag(
      0,
      name,
      descr)

  def _search(query: String) =
    tableTag.filter(_.name like s"%$query%")

  override def findTagById(id: Long): Future[Option[models.Tag]] =
    db.run(_findTagById(id))

  override def createTag(name: String, descr: Option[String]): Future[models.Tag] =
    db.run(_createTag(name, descr))

  override def search(query: String): Future[Seq[models.Tag]] =
    db.run(_search(query).result)

  override def close: Future[Unit] =
    future(db.close)

}
