package fi.helsinki.lib.simplestatsreporter;

import java.util.*;
import java.io.*;
import java.text.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.sql.*;

public class DBReader {

    public static Integer[] readTimes(Statement stmt) throws SQLException {
	ArrayList<Integer> times = new ArrayList<Integer>();
	ResultSet rs;

	rs = stmt.executeQuery("SELECT DISTINCT time " +
			       "FROM downloadspercommunity " +
			       "ORDER BY time");
	while (rs.next()) {
	    times.add(rs.getInt("time"));
	}

	Integer[] retVal = new Integer[times.size()];
	retVal = times.toArray(retVal);
	return retVal;
    }

    public static Hashtable<UUID, Community> readCommunities(Statement stmt)
	throws SQLException {

	Hashtable<UUID, Community> communities = new Hashtable<UUID, Community>();
	ResultSet rs;

	rs = stmt.executeQuery("SELECT * FROM community");
	
	while (rs.next()) {
	    UUID id = UUID.fromString(rs.getString("community_id"));
	    String name = rs.getString("name");
	    String handle = rs.getString("handle");
	    int n_items = rs.getInt("n_items");
	    int n_bitstreams = rs.getInt("n_bitstreams");
	    long n_bytes = rs.getLong("n_bytes");
	    communities.put(id, new Community(id, name, handle,
					      n_items, n_bitstreams, n_bytes));
	}
	return communities;
    }

    public static void communitiesToTrees(Hashtable<UUID, Community>
					  communities, Statement stmt)
	throws SQLException {

	ResultSet rs = stmt.executeQuery("SELECT * FROM community2community");

	while (rs.next()) {
	    Community parent, child;

	    parent = communities.get(UUID.fromString(rs.getString("parent_comm_id")));
	    child = communities.get(UUID.fromString(rs.getString("child_comm_id")));
	    
	    parent.addChild(child);
	}
    }

    public static Hashtable<UUID, Collection> readCollections(Statement stmt)
	throws SQLException {

	Hashtable<UUID, Collection> collections = new Hashtable<UUID, Collection>();
	ResultSet rs;

	rs = stmt.executeQuery("SELECT * FROM collection");
	
	while (rs.next()) {
	    UUID id = UUID.fromString(rs.getString("collection_id"));
	    String name = rs.getString("name");
	    String handle = rs.getString("handle");
	    int n_items = rs.getInt("n_items");
	    int n_bitstreams = rs.getInt("n_bitstreams");
	    long n_bytes = rs.getLong("n_bytes");
	    collections.put(id, new Collection(id, name, handle,
					       n_items, n_bitstreams, n_bytes));
	}
	return collections;
    }

    public static Hashtable<UUID, Item> readItems(Statement stmt)
	throws SQLException {

	Hashtable<UUID, Item> items = new Hashtable<UUID, Item>();
	ResultSet rs;

	rs = stmt.executeQuery("SELECT * FROM item");
	
	while (rs.next()) {
	    UUID id = UUID.fromString(rs.getString("item_id"));
	    String name = rs.getString("name");
	    String handle = rs.getString("handle");
	    int n_items = rs.getInt("n_items");
	    int n_bitstreams = rs.getInt("n_bitstreams");
	    long n_bytes = rs.getLong("n_bytes");
	    items.put(id, new Item(id, name, handle,
				   n_items, n_bitstreams, n_bytes));
	}
	return items;
    }

    public static void setRelations(Statement stmt, Hashtable communities,
				    Hashtable collections, Hashtable items)
	throws SQLException {

	ResultSet rs;

	rs = stmt.executeQuery("SELECT * FROM community2community");
	while (rs.next()) {
	    Community parent, child;

	    parent = (Community)communities.get(UUID.fromString(rs.getString("parent_comm_id")));
	    child = (Community)communities.get(UUID.fromString(rs.getString("child_comm_id")));
	    
	    parent.addChild(child);
	}

	rs = stmt.executeQuery("SELECT * FROM community2collection");
	while (rs.next()) {
	    Community community;
	    Collection collection;

	    community =	(Community)communities.get(UUID.fromString(rs.getString("community_id")));
	    collection = (Collection)collections.get(UUID.fromString(rs.getString("collection_id")));
	    
	    community.addChild(collection);
	}

	rs = stmt.executeQuery("SELECT * FROM collection2item");
	while (rs.next()) {
	    Collection collection;
	    Item item;

	    collection = (Collection)collections.get(UUID.fromString(rs.getString("collection_id")));
	    item = (Item)items.get(UUID.fromString(rs.getString("item_id")));
	    
	    collection.addChild(item);
	}
    }

    /* In the following methods:
       
       count > 0 AND "time" >= startTime and "time" <= stopTime

       is not really needed, it's there just to cut down the number
       of rows (in an attempt to make things faster). */

    public static void readCommunitiesStats(Statement stmt,
					    Hashtable communities,
					    int startTime, int stopTime)
	throws SQLException {

	ResultSet rs;

	rs = stmt.executeQuery("SELECT * FROM downloadspercommunity WHERE " +
			       "count > 0 AND " +
			       "\"time\" >= " + startTime + " AND " +
			       "\"time\" <= " + stopTime);
	
	while (rs.next()) {
	    int time = rs.getInt("time");
	    int count = rs.getInt("count");
	    UUID community_id = UUID.fromString(rs.getString("community_id"));
	    ((Community)communities.get(community_id)).setCounter(time, count);
	}
    }

    public static void readCollectionsStats(Statement stmt,
					    Hashtable collection,
					    int startTime, int stopTime)
	throws SQLException {

	ResultSet rs;

	rs = stmt.executeQuery("SELECT * FROM downloadspercollection WHERE " +
			       "count > 0 AND " +
			       "\"time\" >= " + startTime + " AND " +
			       "\"time\" <= " + stopTime);
	
	while (rs.next()) {
	    int time = rs.getInt("time");
	    int count = rs.getInt("count");
	    UUID collection_id = UUID.fromString(rs.getString("collection_id"));
	    ((Collection)collection.get(collection_id)).setCounter(time,
								   count);
	}
    }

    public static void readItemsStats(Statement stmt, Hashtable item,
				      int startTime, int stopTime)
	throws SQLException {

	ResultSet rs;

	rs = stmt.executeQuery("SELECT * FROM downloadsperitem WHERE " +
			       "count > 0 AND " +
			       "\"time\" >= " + startTime + " AND " +
			       "\"time\" <= " + stopTime);
	
	while (rs.next()) {
	    int time = rs.getInt("time");
	    int count = rs.getInt("count");
	    UUID item_id = UUID.fromString(rs.getString("item_id"));
	    try {
		((Item)item.get(item_id)).setCounter(time, count);
	    }
	    catch (NullPointerException e) {
		// TODO: Add comment.
		;
	    }
	}
    }

    public static void readItemsStatsForCollection(Statement stmt,
						   Hashtable items,
						   UUID collection_id,
						   int startTime, int stopTime)
	throws SQLException {

	ResultSet rs;
	String q;

	q = "SELECT * FROM downloadsperitem WHERE " +
	    "count > 0 AND \"time\" >= " + startTime +
	    " AND \"time\" <= " + stopTime +
	    " AND item_id IN " +
	    "(SELECT item_id FROM collection2item WHERE collection_id = '" +
	    collection_id.toString() + "')";

	rs = stmt.executeQuery(q);
	while (rs.next()) {
	    int time = rs.getInt("time");
	    int count = rs.getInt("count");
	    UUID item_id = UUID.fromString(rs.getString("item_id"));
	    ((Item)items.get(item_id)).setCounter(time, count);
	}
    }

    /* NOTE: Be sure to check the handle before passing it to the methods
       handleToItemId(), handleToCollectionId() and handleToCommunityId(),
       because they don't check it.... so if that string contains something
       like DROP say goodbye to your data!  */

    public static UUID handleToItemId(Statement stmt, String handle)
	throws SQLException {
	return handleToId(stmt, handle, "item");
    }

    public static UUID handleToCollectionId(Statement stmt, String handle)
	throws SQLException {
	return handleToId(stmt, handle, "collection");
    }

    public static UUID handleToCommunityId(Statement stmt, String handle)
	throws SQLException {
	return handleToId(stmt, handle, "community");
    }

    private static UUID handleToId(Statement stmt, String handle, String ob)
	throws SQLException {
	ResultSet rs;
	String q = "SELECT * FROM " + ob + " WHERE handle = '" + handle + "'";
	
	rs = stmt.executeQuery(q);
	return (rs.next() ? UUID.fromString(rs.getString(ob + "_id")) : new UUID(0,0));
    }

    public static Hashtable<String, Integer> statsForItem(Statement stmt, UUID itemId)
	throws SQLException {
	return statsFor(stmt, itemId, "item");
    }

    public static Hashtable<String, Integer> statsForCollection(Statement stmt, UUID collectionId)
	throws SQLException {
	return statsFor(stmt, collectionId, "collection");
    }

    public static Hashtable<String, Integer> statsForCommunity(Statement stmt, UUID communityId)
	throws SQLException {
	return statsFor(stmt, communityId, "community");
    }

    private static Hashtable<String, Integer> statsFor(Statement stmt, UUID obId, String ob)
	throws SQLException {
	Hashtable<String, Integer> stats = new Hashtable<String, Integer>();
	int sum = 0;
	ResultSet rs;
	String q = ("SELECT * FROM " + 
		    "downloadsper" + ob + " WHERE " +
		    ob + "_id = " + obId.toString());
	
	rs = stmt.executeQuery(q);
	while (rs.next()) {
	    String time = Integer.toString(rs.getInt("time"));
	    int count = rs.getInt("count");
	    
	    stats.put(time, count);
	    sum += count;
	}
	stats.put("sum", sum);
	return stats;
    }
    
}

	
	
