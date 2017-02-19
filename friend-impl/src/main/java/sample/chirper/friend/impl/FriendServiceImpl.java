/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.jdbc.JdbcSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import akka.NotUsed;
import sample.chirper.friend.api.FriendId;
import sample.chirper.friend.api.FriendService;
import sample.chirper.friend.api.User;
import sample.chirper.friend.impl.FriendCommand.AddFriend;
import sample.chirper.friend.impl.FriendCommand.CreateUser;
import sample.chirper.friend.impl.FriendCommand.GetUser;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class FriendServiceImpl implements FriendService {

  private final PersistentEntityRegistry persistentEntities;
  private final JdbcSession jdbcSession;

  @Inject
  public FriendServiceImpl(PersistentEntityRegistry persistentEntities, ReadSide readSide,
      JdbcSession jdbcSession) {
    this.persistentEntities = persistentEntities;
    this.jdbcSession = jdbcSession;

    persistentEntities.register(FriendEntity.class);
    readSide.register(FriendEventProcessor.class);
  }

  @Override
  public ServiceCall<NotUsed, User> getUser(String userId) {
    return request -> {
      return friendEntityRef(userId).ask(new GetUser()).thenApply(reply -> {
        if (reply.user.isPresent())
          return reply.user.get();
        else
          throw new NotFound("user " + userId + " not found");
      });
    };
  }

  @Override
  public ServiceCall<User, NotUsed> createUser() {
    return request -> {
      return friendEntityRef(request.userId).ask(new CreateUser(request))
          .thenApply(ack -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<FriendId, NotUsed> addFriend(String userId) {
    return request -> {
      return friendEntityRef(userId).ask(new AddFriend(request.friendId))
          .thenApply(ack -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<NotUsed, PSequence<String>> getFollowers(String userId) {
    return req -> {
      return jdbcSession.withConnection(connection -> {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM follower WHERE userId = ?")) {
          ps.setString(1, userId);
          try (ResultSet rs = ps.executeQuery()) {
            PSequence<String> followers = TreePVector.empty();

            while (rs.next()) {
              followers = followers.plus(
                  rs.getString("followedBy")
              );
            }

            return followers;
          }
        }
      });
    };
  }

  @Override
  public ServiceCall<NotUsed, String> health() {
    return req -> completedFuture("OK");
  }

  private PersistentEntityRef<FriendCommand> friendEntityRef(String userId) {
    PersistentEntityRef<FriendCommand> ref = persistentEntities.refFor(FriendEntity.class, userId);
    return ref;
  }

}
