/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;

import com.lightbend.lagom.javadsl.persistence.jdbc.JdbcReadSide;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.chirper.friend.impl.FriendEvent.FriendAdded;

import javax.inject.Inject;

public class FriendEventProcessor extends ReadSideProcessor<FriendEvent> {

  private final JdbcReadSide readSide;

  @Inject
  public FriendEventProcessor(JdbcReadSide readSide) {
    this.readSide = readSide;
  }

  @Override
  public PSequence<AggregateEventTag<FriendEvent>> aggregateTags() {
    return TreePVector.singleton(FriendEventTag.INSTANCE);
  }

  @Override
  public ReadSideHandler<FriendEvent> buildHandler() {
    return readSide.<FriendEvent>builder("friend_offset")
            .setGlobalPrepare(this::createTable)
            .setEventHandler(FriendAdded.class, this::processFriendChanged)
            .build();
  }

  private void createTable(Connection connection) throws SQLException {
    // @formatter:off
    try (PreparedStatement ps = connection.prepareStatement(
      "CREATE TABLE IF NOT EXISTS follower ( " +
              "userId VARCHAR(64), " +
              "followedBy VARCHAR(64), " +
              "PRIMARY KEY (userId, followedBy)" +
      ")")) {
      ps.execute();
    }
    // @formatter:on
  }

  private void processFriendChanged(Connection connection, FriendAdded event) throws SQLException {

    PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO follower (userId, followedBy) VALUES (?, ?)");
    statement.setString(1, event.friendId);
    statement.setString(2, event.userId);
    statement.execute();
  }

}
