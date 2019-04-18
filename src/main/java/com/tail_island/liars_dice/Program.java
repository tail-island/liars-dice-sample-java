package com.tail_island.liars_dice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Program {
  public static void main(String[] args) {
    new HardHead().execute();
  }
}

class HardHead {
  // 他のプログラムの性格診断をする場合は、コメントを外してください。
  // public void checkOtherPrograms(Career[] careers)
  // {
  //   ;
  // }

  public Action action(Game game) {
    // ログ出力。標準出力は通信で使うので、ログ出力したい場合はSystem.err.printlnを使用してください。
    System.err.println("action() started.");

    // 前のプレイヤーの宣言。
    Bid previousBid = game.getPreviousBid();

    // 確率から推測された、各目の数。
    Map<Integer, Integer> estimatedFaceCounts = game.getEstimatedFaceCounts();

    // もし前のプレイヤーが宣言をしているなら……
    if (previousBid != null) {
      // もし確率よりも大きな数を宣言していたなら……
      if (previousBid.minCount > estimatedFaceCounts.get(previousBid.face)) {
        // チャレンジ！
        return new Action(new Challenge());
      }
    }

    // 宣言候補を作成します。
    Action[] actionCandidates = IntStream.range(2, 7).
      mapToObj(face -> new Action(new Bid(face, estimatedFaceCounts.get(face)))).  // 確率が示す数で宣言して、
      filter(action -> game.isLegalAction(action)).                                // ルールが許す宣言だけ残して、
      toArray(Action[]::new);                                                      // 配列にします。

    // 宣言候補がないなら……
    if (actionCandidates.length == 0) {
      // しょうがないのでチャレンジします。
      return new Action(new Challenge());
    }

    // 宣言候補からランダムで一つ選択します。
    return actionCandidates[new Random().nextInt(actionCandidates.length)];
  }

  // 終了処理がある場合は、コメントを外してください。
  // public void gameEnd(Game game) {
  //   ;
  // }

  public void execute() {
    try {
      boolean isTerminated = false;

      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      ObjectMapper objectMapper = new ObjectMapper();

      while (!isTerminated) {
        String commandString   = reader.readLine();
        String parameterString = reader.readLine();

        switch (commandString) {
        case "check_other_programs":
          // 他のプログラムの性格診断をする場合は、コメントを外してください。
          // checkOtherPrograms(objectMapper.readValue(parameterString, Career[].class));
          System.out.println("OK");

          break;

        case "action":
          String resultString = objectMapper.writeValueAsString(action(objectMapper.readValue(parameterString, Game.class)));
          System.out.println(resultString);

          break;

        case "game_end":
          // 終了処理がある場合は、コメントを外してください。
          // gameEnd(objectMapper.readValue(parameterString, Game.class));
          System.out.println("OK");

          break;

        default:
          isTerminated = true;
          System.out.println("OK");

          break;
        }
      }

    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}

class Career {
  public String id;

  @JsonProperty("career_records")
  public CareerRecord[] careerRecords;
}

class CareerRecord {
  public String id;

  public Game game;
}

class Game {
  public Player[] players;

  @JsonProperty("player_index")
  public int playerIndex;

  public int[] getFaces() {
    return players[playerIndex].faces;
  }

  public int getSecretDiceCount() {
    return Arrays.stream(players).mapToInt(player -> player.faces.length).sum() - getFaces().length;
  }

  public Map<Integer, Integer> getFaceCounts() {
    return IntStream.range(2, 7).boxed().collect(Collectors.toMap(targetFace -> new Integer(targetFace), targetFace -> new Integer((int)Arrays.stream(getFaces()).filter(face -> face == targetFace || face == 1).count())));
  }

  public Map<Integer, Integer> getEstimatedFaceCounts() {
    Map<Integer, Integer> faceCounts = getFaceCounts();
    int secretDiceCount = getSecretDiceCount();

    return IntStream.range(2, 7).boxed().collect(Collectors.toMap(targetFace -> new Integer(targetFace), targetFace -> new Integer(Math.round(secretDiceCount / 3.0f) + faceCounts.get(targetFace))));
  }

  public Player getPreviousPlayer() {
    return players[(playerIndex + players.length - 1) % players.length];
  }

  public Bid getPreviousBid() {
    Player previousPlayer = getPreviousPlayer();

    if (previousPlayer.actions.length == 0) {
      return null;
    }

    return previousPlayer.actions[previousPlayer.actions.length - 1].bid;
  }

  public boolean isLegalAction(Action action) {
    if (action.bid != null) {
      Bid bid = action.bid;

      if (bid.face < 2 || bid.face > 6) {
        return false;
      }

      if (bid.minCount < 1) {
        return false;
      }

      if (bid.minCount > 20) {
        return false;
      }

      Bid previousBid = getPreviousBid();

      if (previousBid != null) {
        if (bid.face <= previousBid.face && bid.minCount <= previousBid.minCount) {
          return false;
        }

        if (bid.face >  previousBid.face && bid.minCount <  previousBid.minCount) {
          return false;
        }
      }

      return true;
    }

    if (action.challenge != null) {
      if (getPreviousBid() == null) {
        return false;
      }

      return true;
    }

    return false;
  }
}

class Player {
  public String id;

  public int[] faces;

  public Action[] actions;
}

class Action {
  public Bid bid;

  public Challenge challenge;

  public Action(Bid bid) {
    this.bid = bid;
  }

  public Action(Challenge challenge) {
    this.challenge = challenge;
  }

  public Action() {
    ;
  }
}

class Bid {
  public int face;

  @JsonProperty("min_count")
  public int minCount;

  public Bid(int face, int minCount) {
    this.face = face;
    this.minCount = minCount;
  }

  public Bid() {
    ;
  }
}

class Challenge {
  public int dummy = 0;
}
