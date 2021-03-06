package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.net.URISyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.math.*;

/**
 * This class is a container for SQLDatabaseEngine which operates baased on database
 * @author Group 16
 */
@Slf4j
public class SQLDatabaseEngine {
	//booking state: 0 isBooking; 1 done; 2 confirmed;
	//offering state: 0 not enough; 1 enough; 2 full; 3 old;

	private java.sql.Timestamp timeInput = new java.sql.Timestamp(new java.util.Date().getTime());
	
	/**
	 * Connects to database
	 * @return	Connection object for database
	 * @throws	URISyntaxException when URI access fails
	 * @throws	SQLException when database is not connected successfully
	 */
	protected Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info ("dbUrl: {}", dbUrl);

		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}
	
	/**
	 * A lot of basic operations based on database
	 * @param	functionID	the id of the function to be called
	 * @param	userID		the id of the line user
	 * @param	stringInput	the input string
	 * @param	intInput		the input integer
	 * @param	timeInput	the input timestamp
	 * @return	the result is true if the opearion successes, otherwise the result is false
	 * @throws	Exception when database is not accessed successfully
	 */
	
	public boolean generalFuction(int functionID, String userID, String stringInput, int intInput, Timestamp timeInput) throws Exception{
		int result=0;
		try {
			Connection connection = getConnection();
			switch(functionID) {
			case Constant.SET_USER_TIME:
        		PreparedStatement stmt1 = connection.prepareStatement("UPDATE line_user SET last_login = ? WHERE id = ?;");
        		stmt1.setTimestamp(1, timeInput);
        		stmt1.setString(2, userID);
        		result=stmt1.executeUpdate();
        		stmt1.close();
	        	break;
			case Constant.SET_USER_STATE:
				PreparedStatement stmt2 = connection.prepareStatement("UPDATE line_user SET state = ? WHERE id = ?;");
				stmt2.setInt(1, intInput);
				stmt2.setString(2, userID);
				result=stmt2.executeUpdate();
				stmt2.close();
	        	break;
			case Constant.SET_USER_NAME:
				PreparedStatement stmt3 = connection.prepareStatement("UPDATE line_user SET name = ? WHERE id = ?;");
				stmt3.setString(1, stringInput);
				stmt3.setString(2, userID);
				result=stmt3.executeUpdate();
				stmt3.close();
	        	break;
			case Constant.SET_USER_PHONE:
				PreparedStatement stmt4 = connection.prepareStatement("UPDATE line_user SET phone_num = ? WHERE id = ?;");
				stmt4.setString(1, stringInput);
				stmt4.setString(2, userID);
				result=stmt4.executeUpdate();
				stmt4.close();
	        	break;
			case Constant.SET_USER_AGE:
				PreparedStatement stmt5 = connection.prepareStatement("UPDATE line_user SET age = ? WHERE id = ?;");
				stmt5.setString(1, stringInput);
				stmt5.setString(2, userID);
				result=stmt5.executeUpdate();
				stmt5.close();
	        	break;
			case Constant.CREATE_USER:
				PreparedStatement stmt6 = connection.prepareStatement("INSERT INTO line_user (id, state, last_login, name) VALUES (?, ?, ?, ?);");
				stmt6.setString(1, userID);
				stmt6.setInt(2, intInput);
				stmt6.setTimestamp(3, timeInput);
				stmt6.setString(4, "null");
				result=stmt6.executeUpdate();
				stmt6.close();
	        	break;
			case Constant.DELETE_USER:
				PreparedStatement stmt7 = connection.prepareStatement("DELETE FROM line_userchoose WHERE user_id = ?;DELETE FROM line_booking WHERE user_id = ?; DELETE FROM line_user WHERE id = ?;");
				stmt7.setString(1, userID);
				stmt7.setString(2, userID);
				stmt7.setString(3, userID);
				result=stmt7.executeUpdate();
				stmt7.close();
	        	break;
			case Constant.TOUR_FOUND:
				PreparedStatement stmt8 = connection.prepareStatement("SELECT * FROM line_tour WHERE id = ?;");
				stmt8.setInt(1, intInput);
				ResultSet rs8 = stmt8.executeQuery();
				if (rs8.next()) {
					result=1;
				}
				rs8.close();
				stmt8.close();
	        	break;
			case Constant.SET_BUFFER_TOUR_ID:
				PreparedStatement stmt9 = connection.prepareStatement("INSERT INTO line_userchoose (user_id, tour_id) VALUES (?, ?);");
				stmt9.setString(1, userID);
				stmt9.setInt(2, intInput);
				result=stmt9.executeUpdate();
				stmt9.close();
	        	break;
			case Constant.DELETE_BUFFER_BOOKING:
				PreparedStatement stmt10 = connection.prepareStatement("DELETE FROM line_userchoose WHERE user_id = ?;");
				stmt10.setString(1, userID);
				result=stmt10.executeUpdate();
				stmt10.close();
	        	break;
			case Constant.DELETE_BOOKING:
				PreparedStatement stmt11 = connection.prepareStatement("DELETE FROM line_booking WHERE user_id = ? AND state = 0;");
				stmt11.setString(1, userID);
				result=stmt11.executeUpdate();
				stmt11.close();
	        	break;
			case Constant.SET_BOOKING_TOUR_OFFERING_ID:
				PreparedStatement stmt12 = connection.prepareStatement("INSERT INTO line_booking (user_id, \"tourOffering_id\",state) VALUES (?, ?, ?);");
				stmt12.setString(1, userID);
				stmt12.setInt(2, intInput);
				stmt12.setInt(3, 0);
				result=stmt12.executeUpdate();
				stmt12.close();
	        	break;
			case Constant.SET_BOOKING_ADULT_NUMBER:
				PreparedStatement stmt13 = connection.prepareStatement("UPDATE line_booking SET adult_num = ? WHERE user_id = ? AND state = 0;");
				stmt13.setInt(1, intInput);
				stmt13.setString(2, userID);
				result=stmt13.executeUpdate();
				stmt13.close();
	        	break;
			case Constant.SET_BOOKING_CHILD_NUMBER:
				PreparedStatement stmt14 = connection.prepareStatement("UPDATE line_booking SET child_num = ? WHERE user_id = ? AND state = 0;");
				stmt14.setInt(1, intInput);
				stmt14.setString(2, userID);
				result=stmt14.executeUpdate();
				stmt14.close();
	        	break;
			case Constant.SET_BOOKING_TODDLER_NUMBER:
				PreparedStatement stmt15 = connection.prepareStatement("UPDATE line_booking SET toddler_num = ? WHERE user_id = ? AND state = 0;");
				stmt15.setInt(1, intInput);
				stmt15.setString(2, userID);
				result=stmt15.executeUpdate();
				stmt15.close();
	        	break;
			case Constant.SET_BOOKING_SPECIAL_REQUEST:
				PreparedStatement stmt16 = connection.prepareStatement("UPDATE line_booking SET special_request = ? WHERE user_id = ? AND state = 0;");
				stmt16.setString(1, stringInput);
				stmt16.setString(2, userID);
				result=stmt16.executeUpdate();
				stmt16.close();
	        	break;
			case Constant.SET_BOOKING_CONFIRMATION:
				PreparedStatement stmt17 = connection.prepareStatement("UPDATE line_booking SET state = 1 WHERE user_id = ? AND state = 0;");
				stmt17.setString(1, userID);
				result=stmt17.executeUpdate();
				stmt17.close();
	        	break;
	        	
			default:
	       		break;
			}
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		if (result!=0)
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the last response time of user
	 * @param id		the id of the line user
	 * @param time	the time stamp to be set
	 * @return	the result is true if the setting is called successfully, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setUserTime(String id, java.sql.Timestamp time) throws Exception{
		return generalFuction(Constant.SET_USER_TIME, id, "", 0, time);
	}
	
	/**
	 * Sets the state of user
	 * @param id		the id of the line user
	 * @param state	the user state to be set
	 * @return	the result is true if the setting is called successfully, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setUserState(String id, int state) throws Exception{
		return generalFuction(Constant.SET_USER_STATE, id, "", state, timeInput);
	}
	
	/**
	 * Sets the name of user
	 * @param id		the id of the line user
	 * @param text	the user name to be set
	 * @return	the result is true if the setting is called successfully, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setUserName(String id, String text) throws Exception{
		return generalFuction(Constant.SET_USER_NAME, id, text, 0, timeInput);
	}
	
	/**
	 * Sets the phone number of user
	 * @param id		the id of the line user
	 * @param text	the user's phone number to be set
	 * @return	the result is true if the setting is called successfully, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setUserPhoneNum(String id, String text) throws Exception{
		return generalFuction(Constant.SET_USER_PHONE, id, text, 0, timeInput);
	}
	
	/**
	 * Sets the age of user
	 * @param id		the id of the line user
	 * @param text	the user age to be set
	 * @return	the result is true if the setting is called successfully, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setUserAge(String id, String text) throws Exception{
		return generalFuction(Constant.SET_USER_AGE, id, text, 0, timeInput);
	}
	
	/**
	 * Creates user in database
	 * @param id		the id of the line user
	 * @param time	the last response time of user to be set
	 * @param state	the user state to be set
	 * @return	the result is true if the creation is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean createUser(String id, java.sql.Timestamp time, int state) throws Exception{
		return generalFuction(Constant.CREATE_USER, id, "", state, time);
	}
	
	/**
	 * Deletes user in database
	 * @param id		the id of the line user
	 * @return	the result is true if the deletion is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean deleteUser(String id)  throws Exception{
		return generalFuction(Constant.DELETE_USER, id, "", 0, timeInput);
	}
	
	/**
	 * Finds the tour with tour ID
	 * @param tourID		the id of the tour
	 * @return	the result is true if the tour is found successfully, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */	
	public boolean tourFound(int tourID) throws Exception{
		return generalFuction(Constant.TOUR_FOUND, "", "",tourID , timeInput);
	}
	
	/**
	 * Sets the buffering tour with tour ID
	 * @param userID		the id of the line user
	 * @param tourID		the id of the tour
	 * @return	the result is true if the buffering tour is set successfully, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */	
	public boolean setBufferTourID(String userID, int tourID) throws Exception{
		return generalFuction(Constant.SET_BUFFER_TOUR_ID, userID, "",tourID , timeInput);
	}
	
	/**
	 * Deletes the buffering booking entry with userID
	 * @param userID		the id of the line user
	 * @return	the result is true if the deletion is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */	
	public boolean deleteBufferBookingEntry(String userID) throws Exception{
		return generalFuction(Constant.DELETE_BUFFER_BOOKING, userID, "",0, timeInput);
	}

	/**
	 * Deletes the booking entry with userID
	 * @param userID		the id of the line user
	 * @return	the result is true if the deletion is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */	
	public boolean deleteBookingEntry(String userID) throws Exception{
		return generalFuction(Constant.DELETE_BOOKING, userID, "",0, timeInput);
	}
	
	/**
	 * Sets the id of the tour offering for booking
	 * @param userID		the id of the line user
	 * @param tourOfferingID		the id os the tour offering
	 * @return	the result is true if the setting is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setBookingTourOfferingID(String userID, int tourOfferingID) throws Exception{
		return generalFuction(Constant.SET_BOOKING_TOUR_OFFERING_ID, userID, "",tourOfferingID, timeInput);
	}
	
	/**
	 * Sets the number of adults for booking
	 * @param userID		the id of the line user
	 * @param number		the number of adults to be set
	 * @return	the result is true if the setting is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setBookingAdultNumber(String userID,int number) throws Exception{
		return generalFuction(Constant.SET_BOOKING_ADULT_NUMBER, userID, "",number, timeInput);
	}
	
	/**
	 * Sets the number of children for booking
	 * @param userID		the id of the line user
	 * @param number		the number of children to be set
	 * @return	the result is true if the setting is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setBookingChildrenNumber(String userID,int number) throws Exception{
		return generalFuction(Constant.SET_BOOKING_CHILD_NUMBER, userID, "",number, timeInput);
	}
	
	/**
	 * Sets the number of toddlers for booking
	 * @param userID		the id of the line user
	 * @param number		the number of toddlers to be set
	 * @return	the result is true if the setting is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setBookingToddlerNumber(String userID,int number) throws Exception{
		return generalFuction(Constant.SET_BOOKING_TODDLER_NUMBER, userID, "",number, timeInput);
	}
	
	/**
	 * Sets the special request from user for booking
	 * @param userID		the id of the line user
	 * @param request	the special request to be set
	 * @return	the result is true if the setting is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setBookingSpecialRequest(String userID,String request) throws Exception{
		return generalFuction(Constant.SET_BOOKING_SPECIAL_REQUEST, userID, request,0, timeInput);
	}
	
	/**
	 * Sets the confirmation for user after booking
	 * @param userID		the id of the line user
	 * @return	the result is true if the setting is successful, otherwise the result is false
	 * @throws	Exception when calling generalFuction fails
	 */
	public boolean setBookingConfirmation(String userID) throws Exception{
		return generalFuction(Constant.SET_BOOKING_CONFIRMATION, userID, "",0, timeInput);
	}
	
	/**
	 * Gets the user information given user ID
	 * @param id		the id of the line user
	 * @return 		the user obeject given the user ID
	 * @see User
	 */
	public User getUserInformation(String id){
		User result=new User();
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM line_user WHERE id = ?;");
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
					result.setID(rs.getString(1));
					result.setName(rs.getString(2));
					result.setPhoneNumber(rs.getString(3));
					result.setAge(rs.getString(4));
					result.setState(rs.getInt(5));
					result.setTime(rs.getTimestamp(6));
					// train offering
			}
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		}
		return result;
	}
	
	/**
	 * Adds unknown question to unknown datatabse
	 * @param text	the unknown questoin
	 * @return	the result is true if the addition is successful, otherwise the result is false
	 * @throws	Exception when database is not accessed successfully
	 */
	
	public boolean addToUnknownDatatabse(String text) throws Exception{
		int result=0;
		int min_dist = 5;
		String resultString = null;
		int dist = min_dist + 1;
		try {
			Connection connection = getConnection();
			//check all entries in databse compare and calculate distance ,update hit numebr
			PreparedStatement stmt = connection.prepareStatement("SELECT line_unknownquestion.question, line_unknownquestion.hit FROM line_unknownquestion;");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				resultString = rs.getString(1);//get question
				dist=new WagnerFischer(resultString,text).getDistance();
				if(dist <= min_dist) {//similar question
					PreparedStatement stmt1 = connection.prepareStatement("UPDATE line_unknownquestion SET hit = ? WHERE question = ?;");
					int hit = rs.getInt(2)+1;
					stmt1.setInt(1, hit);
					stmt1.setString(2,resultString);
					result = stmt1.executeUpdate();
					stmt1.close();
					break;
				}
			}
			if(dist > min_dist ) {
				//insert new entry
				PreparedStatement stmt2 = connection.prepareStatement("INSERT INTO line_unknownquestion (question,hit) VALUES (?, ?);");
				stmt2.setString(1, text);
				stmt2.setInt(2, 1);
				result = stmt2.executeUpdate();
				stmt2.close();	
			}
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		if (result!=0)
			return true;
		else
			return false;
	}
	/**
	 * Gets current available tours
	 * @return list of current available tours
	 * @see Tour
	 * @throws	Exception when database is not accessed successfully
	 */
	
	public List<Tour> getTours()throws Exception {
		List<Tour> listOfTours = new ArrayList<Tour>();
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM line_tour;");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
		    	Tour tour=new Tour(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(7),rs.getString(8));
		    	listOfTours.add(tour);
			}
			rs.close();
			stmt.close();
			connection.close();	
		} catch (Exception e) {
			log.info(e.toString());
		} 
		if (listOfTours != null && !listOfTours.isEmpty())
			return listOfTours;
		log.info("tour database probably empty");
		throw new Exception("EMPTY DATABASE");
	}

	/**
	 * Gets available tour offerings
	 * @param tourID		the id of the given tour
	 * @return list of current available tour offerings
	 * @see TourOffering
	 * @throws	Exception when database is not accessed successfully
	 */

	public List<TourOffering> displayTourOffering(int tourID) throws Exception{
		List<TourOffering> listOfTourOfferings = new ArrayList<TourOffering>();
		String result="";
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT line_touroffering.id,line_touroffering.offer_date,line_touroffering.hotel,line_touroffering.capacity_max, "
					+ "line_touroffering.price, line_tour.duration, "
					+ "SUM (line_booking.adult_num+line_booking.child_num+line_booking.toddler_num) "
					+ "FROM line_tour, line_touroffering LEFT JOIN line_booking ON line_touroffering.id=line_booking.\"tourOffering_id\" "
					+ "AND line_booking.state>0 "
					+ "WHERE line_touroffering.tour_id = ? "
					+ "AND line_touroffering.state < 2 AND line_touroffering.tour_id=line_tour.id "
					+ "AND line_touroffering.id IN "
					+ "(SELECT id FROM line_touroffering WHERE tour_id = ? EXCEPT SELECT line_touroffering.id "
					+ "FROM line_touroffering, line_booking WHERE line_touroffering.tour_id = ? "
					+ "AND line_touroffering.id=line_booking.\"tourOffering_id\" AND line_booking.state>0 "
					+ "GROUP BY line_touroffering.id "
					+ "HAVING SUM (line_booking.adult_num+line_booking.child_num+line_booking.toddler_num) >= "
					+ "line_touroffering.capacity_max) "
					+ "GROUP BY line_touroffering.id,line_touroffering.offer_date,line_touroffering.hotel, "
					+ "line_touroffering.capacity_max,  line_touroffering.price, line_tour.duration;");
			stmt.setInt(1, tourID);
			stmt.setInt(2, tourID);
			stmt.setInt(3, tourID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Timestamp time=rs.getTimestamp(2);
				String time_change_2=""+time;
				String time_change=time_change_2.substring(0, 12)+'8'+time_change_2.substring(13);
				double price=rs.getDouble(5);
				int price_int = (int)price;
				int quota = -rs.getInt(7)+rs.getInt(4);
				TourOffering tourOffering=new TourOffering(rs.getInt(1),time_change,rs.getString(3),rs.getInt(4),price_int,rs.getInt(6),quota);
				listOfTourOfferings.add(tourOffering);
			}
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		return listOfTourOfferings;
	}
	
	
	/**
	 * Gets buffering tour ID
	 * @param userID		the ID of the line user
	 * @return buffering tour ID given the user ID
	 * @throws	Exception when database is not accessed successfully
	 */
	public int getBufferTourID(String userID) throws Exception{
		int result=-1;
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement("SELECT * FROM line_userchoose WHERE user_id = ?;");
			stmt.setString(1, userID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
					result=rs.getInt(2);
			}
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		return result;
	}

	/**
	 * Checks tour ID and tour offering ID are matched
	 * @param tourID		the ID of the tour
	 * @param tourOfferingID		the ID of the tour offering
	 * @return	the result is true if tour ID and tour offering ID are matched, otherwise the result is false
	 * @throws	Exception when database is not accessed successfully
	 */

	public boolean tourOfferingFound(int tourID,int tourOfferingID) throws Exception{
		boolean result=false;
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT id,offer_date,hotel,capacity_max FROM line_touroffering WHERE tour_id = ? AND state < 2 AND id = ? AND id IN "
					+ "(SELECT id FROM line_touroffering WHERE tour_id = ? "
					+ "EXCEPT SELECT line_touroffering.id "
					+ "FROM line_touroffering, line_booking "
					+ "WHERE line_touroffering.tour_id = ? "
					+ "AND line_touroffering.id=line_booking.\"tourOffering_id\" "
					+ "AND line_booking.state>0 "
					+ "GROUP BY line_touroffering.id "
					+ "HAVING SUM (line_booking.adult_num+line_booking.child_num+line_booking.toddler_num) >= "
					+ "line_touroffering.capacity_max);");
			stmt.setInt(1, tourID);
			stmt.setInt(2, tourOfferingID);
			stmt.setInt(3, tourID);
			stmt.setInt(4, tourID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				result=true;
			}
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		return result;
	}
	/**
	 * Gets quota for booking of the user
	 * @param userID		the id of the line user
	 * @return the remaining quota for the user
	 * @throws	Exception when database is not accessed successfully
	 */
	public int checkQuota(String userID) throws Exception{
		int result=-1;
		int max=0;
		int sum=0;
		int adult=0;
		int child=0;
		int toddler=0;
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT SUM (line_booking.adult_num+line_booking.child_num+line_booking.toddler_num), line_touroffering.capacity_max "
					+ "FROM line_tour,line_touroffering LEFT JOIN line_booking ON line_touroffering.id=line_booking.\"tourOffering_id\" "  
					+ "AND line_booking.state>0 "
					+ "WHERE line_touroffering.state < 2 "
					+ "AND line_touroffering.tour_id=line_tour.id "
					+ "AND line_touroffering.id IN "
					+ "(SELECT line_booking.\"tourOffering_id\" FROM line_booking WHERE user_id = ? AND state = 0) "
					+ "GROUP BY line_touroffering.id,line_touroffering.capacity_max;");
			stmt.setString(1, userID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				sum=rs.getInt(1);
				max=rs.getInt(2);
			}
			rs.close();
			stmt.close();
			
			PreparedStatement stmt2 = connection.prepareStatement(
					"SELECT line_booking.adult_num, line_booking.child_num, line_booking.toddler_num "
					+ "FROM line_booking WHERE user_id = ? AND state = 0;");
			stmt2.setString(1, userID);
			ResultSet rs2 = stmt2.executeQuery();
			if (rs2.next()) {
				adult=rs2.getInt(1);
				child=rs2.getInt(2);
				toddler=rs2.getInt(3);
			}
			rs2.close();
			stmt2.close();
			result=max-sum-adult-child-toddler;
			if (result<0) {
				PreparedStatement stmt3 = connection.prepareStatement("UPDATE line_booking SET adult_num = ?, child_num = ?, toddler_num = ? "
						+ "WHERE user_id = ? AND state = 0;");
				stmt3.setInt(1, 0);
				stmt3.setInt(2, 0);
				stmt3.setInt(3, 0);
				stmt3.setString(4, userID);
				stmt3.executeUpdate();
				stmt3.close();
			}
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		return result;
	}
	
	/**
	 * Displays booking information given user ID
	 * @param userID		the id of the line user
	 * @return the booking information of a particular user
	 * @throws	Exception when database is not accessed successfully
	 */
	public String displaytBookingInformation(String userID) throws Exception{
		String result="";
		int result2=0;
		String special="";
		int adult=0;
		int child=0;
		int toddler=0;
		double fee=0;
		double price=0;
		
		String discount_name="";
		int discount_id=0;
		int booking_id=0;
		int seat=0;
		int seat_for_adult=0;
		int seat_for_child=0;
		double rate=0.0;
		double discount_fee=0.0;
		double total_fee=0.0;
		int discount_fee_int=0;
		int total_fee_int=0;
		int fee_int=0;
		
		boolean has_discount= false;
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT line_booking.adult_num, line_booking.child_num, line_booking.toddler_num, line_touroffering.price, "
					+ "line_booking.special_request, line_touroffering.offer_date, line_touroffering.hotel, "
					+ "line_touroffering.capacity_max, line_touroffering.guide_name, line_touroffering.guide_line, "
					+ "line_tour.name, line_tour.description, line_tour.duration FROM line_booking, "
					+ "line_touroffering, line_tour WHERE line_booking.user_id = ? AND line_booking.state = 0 AND "
					+ "line_booking.\"tourOffering_id\"=line_touroffering.id AND line_touroffering.tour_id=line_tour.id;");
			stmt.setString(1, userID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				adult=rs.getInt(1);
				child=rs.getInt(2);
				toddler=rs.getInt(3);
				price=rs.getDouble(4);
				special=rs.getString(5);
				Timestamp time=rs.getTimestamp(6);
				String time_change_2=""+time;
				String time_change=time_change_2.substring(0, 12)+'8'+time_change_2.substring(13);
				fee = price*adult + price*0.8*child;
				fee_int = (int)fee;
				total_fee = fee;
				total_fee_int = (int)total_fee;
				
				PreparedStatement stmt2 = connection.prepareStatement(
						"SELECT line_discount.name, line_discount.id, line_booking.id, line_discount.seat, line_discount.rate FROM line_booking, "
						+ "line_touroffering, line_discount WHERE line_booking.user_id = ? AND line_booking.state = 0 AND "
						+ "line_booking.\"tourOffering_id\"=line_touroffering.id AND line_touroffering.id=line_discount.\"tourOffering_id\" "
						+ "AND line_discount.id IN "
						+ "(SELECT line_discount.id FROM line_discount LEFT JOIN line_booking ON line_booking.discount_id=line_discount.id "
						+ "GROUP BY line_discount.id "
						+ "HAVING COUNT(line_booking.discount_id=line_discount.id) < line_discount.quota);");
				stmt2.setString(1, userID);
				ResultSet rs2 = stmt2.executeQuery();
				if (rs2.next()) {
					has_discount= true;
					discount_name=rs2.getString(1);
					discount_id=rs2.getInt(2);
					booking_id=rs2.getInt(3);
					seat=rs2.getInt(4);
					rate=rs2.getDouble(5);
					result+=("Congratulations! You gain a "+discount_name+" for "+seat+" seats in this booking.\n"
							+ "If you canceled this booking, you will lose this discount.\n\n");
					discount_fee = calculateDiscount(seat, adult,discount_fee, rate,price,
							seat_for_adult, child, seat_for_child);
					total_fee = fee-discount_fee;
					discount_fee_int = (int)discount_fee;
					total_fee_int = fee_int-discount_fee_int ;
				}
					
				result+=("Tour name: "+rs.getString(11)+"\n\nDescription: "+rs.getString(12)+"\n\nDuration: "+rs.getInt(13)+"\n\nOffer date: "
				+time_change+"\n\nHotel: "+rs.getString(7)+"\n\nMax people: "+rs.getInt(8)+"\n\nGuide name: "+rs.getString(9)
				+"\n\nGuide line account: "+rs.getString(10)+"\n\nAdult: "+adult+"\n\nChild: "+child+"\n\nToddler: "+toddler
				+"\n\nSpecial request: "+special);
				if(has_discount) {
					result+=("\n\nOriginal fee: HKD "+fee_int+"\n\nTotal dicount fee: HKD "+discount_fee_int+
							"\n\nTotal fee: HKD "+total_fee_int);
				}
				else {
					result+=("\n\nTotal fee: HKD "+total_fee_int+"\n\n");
				}
				rs2.close();
				stmt2.close();
			}
			rs.close();
			stmt.close();
			if(has_discount) {
				PreparedStatement stmt2 = connection.prepareStatement(
						"UPDATE line_booking SET tour_fee = ?, paid_fee = ?, discount_id = ? WHERE user_id = ? AND state = 0;");
				stmt2.setDouble(1, total_fee);
				stmt2.setDouble(2, 0.0);
				stmt2.setInt(3, discount_id);
				stmt2.setString(4, userID);
				result2=stmt2.executeUpdate();
				stmt2.close();
			}
			else {
			PreparedStatement stmt2 = connection.prepareStatement(
					"UPDATE line_booking SET tour_fee = ?, paid_fee = ? WHERE user_id = ? AND state = 0;");
			stmt2.setDouble(1, total_fee);
			stmt2.setDouble(2, 0.0);
			stmt2.setString(3, userID);
			result2=stmt2.executeUpdate();
			stmt2.close();
			}
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		if (result == "")
			return "null";
		else
			return result;
	}
	
	/**
	 * Calculates the discount fee if a user gets the discount quota
	 * @param seat	the number of seats of the discount campaign
	 * @param adult  the number of adults
	 * @param discount_fee the fee to be calculated
	 * @param rate 	the discount rate
	 * @param price	the full price
	 * @param seat_for_adult the number of seats for aults
	 * @param child 	the number of children
	 * @param seat_for_child the numeber of seats for children
	 * @return the discount fee
	 */

	public double calculateDiscount(int seat, int adult,double discount_fee, double rate,double price,
			int seat_for_adult, int child, int seat_for_child) {
		if(seat==0) {
			//pass
		}
		else if (adult>=seat && seat>0) {
			discount_fee += seat*(1.0-rate)*price;
			seat_for_adult=seat;
		}
		else {
			discount_fee += adult*(1.0-rate)*price;
			seat_for_adult=adult;
			if(child>=(seat-seat_for_adult)) {
				discount_fee += (seat-seat_for_adult)*(1.0-rate)*price*0.8;
				seat_for_child=(seat-seat_for_adult);
			}
			else{
				discount_fee += child*(1.0-rate)*price;
				seat_for_child = child;
			}
		}
		return discount_fee;
	}
	/**
	 * Reviews booking information given user ID
	 * @param userID		the id of the line user
	 * @return the booking information of a particular user
	 * @throws	Exception when database is not accessed successfully
	 */
	public String reviewBookingInformation(String userID) throws Exception{
		String result="";
		int result2=0;
		double fee=0;
		double paid_fee=0;
		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT line_booking.adult_num, line_booking.child_num, line_booking.toddler_num, line_booking.tour_fee, "
					+ "line_booking.special_request, line_touroffering.offer_date, line_touroffering.hotel, "
					+ "line_touroffering.capacity_max, line_touroffering.guide_name, line_touroffering.guide_line, "
					+ "line_tour.name, line_tour.description, line_tour.duration, line_booking.state, line_booking.paid_fee FROM line_booking, "
					+ "line_touroffering, line_tour WHERE line_booking.user_id = ? AND line_booking.state > 0 AND "
					+ "line_booking.\"tourOffering_id\"=line_touroffering.id AND line_touroffering.tour_id=line_tour.id;");
			stmt.setString(1, userID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Timestamp time=rs.getTimestamp(6);
				String time_change_2=""+time;
				String time_change=time_change_2.substring(0, 12)+'8'+time_change_2.substring(13);
				
				result+=("Tour name: "+rs.getString(11)+"\n\nDescription: "+rs.getString(12)+"\n\nDuration: "+rs.getInt(13)+"\n\nOffer date: "
				+time_change+"\n\nHotel: "+rs.getString(7)+"\n\nMax people: "+rs.getInt(8)+"\n\nGuide name: "+rs.getString(9)
				+"\n\nGuide line account: "+rs.getString(10)+"\n\nAdult: "+rs.getInt(1)+"\n\nChild: "+rs.getInt(2)+"\n\nToddler: "+rs.getInt(3)
				+"\n\nSpecial request: "+rs.getString(5));
				fee=rs.getDouble(4);
				paid_fee=rs.getDouble(15);
				int fee_int = (int)fee;
				int need_to_pay=(int)(fee-paid_fee);
				int state=rs.getInt(14);
				if(state==2) {
					result+=("\n\nTotal fee: HKD "+fee_int+"  \n\nPAID \n\n");
				}
				else {
					result+=("\n\nNeed to pay: HKD "+need_to_pay+"  \n\nUNPAID \n\n");
				}
				result+=("=====\n\n\n\n");
			}
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			log.info(e.toString());
		} 
		if (result == "")
			return "null";
		else
			return result;
	}
}
