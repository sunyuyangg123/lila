package lila.puzzle

import play.api.libs.json._

import lila.common.PimpedJson._
import lila.game.{ Game, GameRepo, PerfPicker }
import lila.tree.Node.partitionTreeJsonWriter

private final class GameJson(
  lightUser: lila.common.LightUser.Getter) {

  case class CacheKey(gameId: Game.ID, plies: Int)

  private val cache = lila.memo.AsyncCache[CacheKey, JsObject](
    name = "puzzle.gameJson",
    f = generate,
    maxCapacity = 500)

  def apply(gameId: Game.ID, plies: Int): Fu[JsObject] = cache(CacheKey(gameId, plies))

  def generate(ck: CacheKey): Fu[JsObject] = ck match {
    case CacheKey(gameId, plies) =>
      (GameRepo game gameId).flatten(s"Missing puzzle game $gameId!") map { game =>
        val perfType = lila.rating.PerfType orDefault PerfPicker.key(game)
        val tree = TreeBuilder(game, plies)
        Json.obj(
          "id" -> game.id,
          "clock" -> game.clock.map(_.show),
          "perf" -> Json.obj(
            "icon" -> perfType.iconChar.toString,
            "name" -> perfType.name),
          "rated" -> game.rated,
          "players" -> JsArray(game.players.map { p =>
            Json.obj(
              "userId" -> p.userId,
              "name" -> lila.game.Namer.playerText(p, withRating = true)(lightUser),
              "color" -> p.color.name
            )
          }),
          "treeParts" -> partitionTreeJsonWriter.writes(tree)
        ).noNull
      }
  }
}
