import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ReviewServlet", value = "/review/*")
public class ReviewServlet extends HttpServlet {
  private final static String EXCHANGE_NAME = "review";
  private final RabbitMQConnection rabbitMQConnection = new RabbitMQConnection();
  private ConcurrentLinkedDeque<Channel> channelPool;

  static class LikeStatus {
    String likes;
    String dislikes;
  }


  @Override
  public void init() {
    String host = "localhost";
    try {
      channelPool = rabbitMQConnection.createChannelPool(host);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void destroy() {
    try {
      rabbitMQConnection.closeChannelPool(channelPool);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    try {
      res.setContentType("application/json");
      String urlPath = req.getPathInfo();
      if (urlPath == null || urlPath.isEmpty()) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write(new Gson().toJson("URL missing parameter."));
        return;
      }

      String[] urlParts = urlPath.split("/");
      if (!isGetUrlValid(urlParts)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write(new Gson().toJson("URL format is incorrect."));
        return;
      } else {
        String albumId = urlParts[1];
        try (Connection connection = DatabaseConnectionPool.getConnection()) {
          String sql= "SELECT likes, dislikes FROM albums WHERE album_id = ?";
          try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, albumId);
            try (ResultSet resultSet = statement.executeQuery()) {
              if (resultSet.next()) {
                LikeStatus likeStatus = new LikeStatus();
                likeStatus.likes = resultSet.getString("likes");
                likeStatus.dislikes = resultSet.getString("dislikes");
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write(new Gson().toJson(likeStatus));
              } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write(new Gson().toJson("Album not found."));
              }
            }
          }

        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }



  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    try {
      res.setContentType("application/json");
      String urlPath = req.getPathInfo();
      if (urlPath == null || urlPath.isEmpty()) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write(new Gson().toJson("URL missing parameter."));
        return;
      }

      String[] urlParts = urlPath.split("/");
      if (!isUrlValid(urlParts)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write(new Gson().toJson("URL format is incorrect."));
        return;
      } else {
        String action = urlParts[1];
        String albumId = urlParts[2];
        Channel channel = null;
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("albumId", albumId);
        messageMap.put("action", action);
        String message = new Gson().toJson(messageMap);
        System.out.println(message);

        try {
          channel = channelPool.removeFirst();
          channel.basicPublish(EXCHANGE_NAME, action, null, message.getBytes("UTF-8"));
          System.out.println(" [x] Sent '" + action + "':'" + message + "'");
          res.setStatus(HttpServletResponse.SC_OK);
          res.getWriter().write(new Gson().toJson("Review submitted."));
        } catch (Exception e) {
          e.printStackTrace();
          res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          res.getWriter().write(new Gson().toJson("Review failed to submit."));
        } finally {
          channelPool.addLast(channel);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    if (urlPath.length != 3) {
      return false;
    }
    String action = urlPath[1];
    if (!action.equals("like") && !action.equals("dislike")) {
      return false;
    }
    String albumId = urlPath[2];
    try {
      Integer.parseInt(albumId);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  private boolean isGetUrlValid(String[] urlPath) {
    if (urlPath.length != 2) {
      return false;
    }
    String albumId = urlPath[1];
    try {
      Integer.parseInt(albumId);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

}
