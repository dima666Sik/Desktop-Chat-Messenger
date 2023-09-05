package ua.desktop.chat.messenger.parser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ua.desktop.chat.messenger.env.TypeMessage;
import ua.desktop.chat.messenger.models.Message;
import ua.desktop.chat.messenger.models.User;

import java.io.Serializable;
import java.util.Optional;

public class ParserJSON {

    // Перетворення об'єкта в JSON рядок
    public static <T> String convertObjectToString(T myObject, TypeMessage typeMessage) {
        Gson gson = new Gson();
        String json = gson.toJson(myObject);
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        jsonObject.addProperty("type", typeMessage.name());
        return jsonObject.toString();
    }

    // Перетворення JSON рядка в об'єкт
    public static Object convertStringToObject(String jsonString) {
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        String typeValue = jsonObject.get("type").getAsString();
        Gson gson = new Gson();
        if (typeValue.equals(TypeMessage.USER_OBJECT.name())) {
            return gson.fromJson(jsonString, User.class);
        } else if (typeValue.equals(TypeMessage.MESSAGE_OBJECT.name())) {
            return gson.fromJson(jsonString, Message.class);
        } else if (typeValue.equals(TypeMessage.STRING_NOTIFICATION.name())) {
            return gson.fromJson(jsonString, String.class);
        }
        throw new RuntimeException("Type Message is unknown! Please choose your type message on valid type...");
    }

}