package ua.desktop.chat.messenger.parser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ua.desktop.chat.messenger.dto.MessageDTO;
import ua.desktop.chat.messenger.dto.UserDTO;
import ua.desktop.chat.messenger.env.TypeMessage;

public class ParserJSON {

    /**
     * Convert object to JSON row
     */
    public synchronized static <T> String convertObjectToString(T myObject, TypeMessage typeMessage) {
        Gson gson = new Gson();
        String json = gson.toJson(myObject);
        JsonObject jsonObject;
        if (typeMessage == TypeMessage.STRING_NOTIFICATION) {
            jsonObject = new JsonObject();
            jsonObject.addProperty("message", (String) myObject);
            jsonObject.addProperty("type", typeMessage.name());
        } else {
            jsonObject = JsonParser.parseString(json).getAsJsonObject();
        }
        jsonObject.addProperty("type", typeMessage.name());
        return jsonObject.toString();
    }

    /**
     * Convert JSON row to object
     */
    public synchronized static Object convertStringToObject(String jsonString) {
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String typeValue = jsonObject.get("type").getAsString();
        Gson gson = new Gson();
        if (typeValue.equals(TypeMessage.USER_OBJECT.name())) {
            return gson.fromJson(jsonString, UserDTO.class);
        } else if (typeValue.equals(TypeMessage.MESSAGE_OBJECT.name())) {
            return gson.fromJson(jsonString, MessageDTO.class);
        } else if (typeValue.equals(TypeMessage.STRING_NOTIFICATION.name())) {
            return jsonObject.get("message").getAsString();
        }
        throw new RuntimeException("Type Message is unknown! Please choose your type message on valid type...");
    }

}