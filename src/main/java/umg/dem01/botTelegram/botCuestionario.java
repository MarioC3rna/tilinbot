package umg.dem01.botTelegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import umg.dem01.model.User;
import umg.dem01.model.userCuestionario;
import umg.dem01.service.UserService;
import umg.dem01.service.serviceCuestionario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class botCuestionario extends TelegramLongPollingBot {



    private Map<Long, String> estadoConversacion = new HashMap<>();
    User usuarioConectado = null;
    UserService userService = new UserService();
    private final Map<Long, Integer> indicePregunta = new HashMap<>();
    private final Map<Long, String> seccionActiva = new HashMap<>();
    private final Map<String, String[]> preguntas = new HashMap<>();
    Boolean enviar = false;


    @Override
    public String getBotUsername() {
        return "@Tilinzanbot";
    }

    @Override
    public String getBotToken() {
        return "7356487611:AAFV-QYv_mYaYdtXbLUfR3CMU1qkqNLLBV4";
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Message message = update.getMessage();
            //obtener el nombre y apellido del usuario en una variable
            String userFirstName = update.getMessage().getFrom().getFirstName();
            String userLastName = update.getMessage().getFrom().getLastName();
            String nickName = update.getMessage().getFrom().getUserName();
            long chat_id = update.getMessage().getChatId();

            try {
                String state = estadoConversacion.getOrDefault(chat_id, "");
                usuarioConectado = userService.getUserByTelegramId(chat_id);


                if (usuarioConectado == null && state.isEmpty()) {
                    sendText(chat_id, "Hola " + formatUserInfo(userFirstName, userLastName) + ", No estas registrado en el sistema. Por favor ingresa tu correo electrónico:");
                    estadoConversacion.put(chat_id, "ESPERANDO_CORREO");
                    return;}
                else if (!messageText.equals("/menu") && !seccionActiva.containsKey(chat_id)){
                    sendText(chat_id, "Hola " + formatUserInfo(userFirstName, userLastName) + ", Envia /menu para comenzar");}

                if (messageText.equals("/menu")) {
                    sendMenu(chat_id);
                } else if (seccionActiva.containsKey(chat_id)) {
                    manejaCuestionario(chat_id, messageText,formatUserInfo(userFirstName,userLastName));
                }


                if (state.equals("ESPERANDO_CORREO")) {
                    processEmailInput(chat_id, messageText);
                    return;
                }
            }catch (Exception e){
                sendText(chat_id, "Algo salio mal al enviar recibir tu mensaje .");
            }}

        else if (update.hasCallbackQuery()) { //es una respusta de un boton
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            inicioCuestionario(chatId, callbackData);
        }
    }

    private void sendMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Dale click a una Seccion");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Crea los botones del menú
        rows.add(crearFilaBoton("Sección 1", "SECTION_1"));
        rows.add(crearFilaBoton("Sección 2", "SECTION_2"));
        rows.add(crearFilaBoton("Sección 3", "SECTION_3"));
        rows.add(crearFilaBoton("Sección 4", "SECTION_4"));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private List<InlineKeyboardButton> crearFilaBoton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }

    private void inicioCuestionario(long chatId, String section) {
        seccionActiva.put(chatId, section);
        indicePregunta.put(chatId, 0);
        enviarPregunta(chatId);
    }

    private void enviarPregunta(long chatId) {
        String seccion = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        String[] questions = preguntas.get(seccion);

        if (index < questions.length) {
            sendText(chatId, questions[index]);
        } else {
            sendText(chatId, "Terminaste el cuestionario O7");
            seccionActiva.remove(chatId);
            indicePregunta.remove(chatId);
        }
    }

    private void manejaCuestionario(long chatId, String response,String nombre) {
        String section = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        if (indicePregunta.get(chatId) == 1 ) {
            int intresponse = Integer.parseInt(response);
            if (intresponse<5){
                sendText(chatId, "Esta fue tu respuesta: " + response);
                sendText(chatId, "Esa no es tu edad\nPorfa Coloque una edad normal");
                enviarPregunta(chatId);
            } else if (intresponse>95) {
                sendText(chatId, "Esta es tu Respuesta: " + response);
                sendText(chatId,"Esa no es tu edad \nPorfa coloque una edad normal");
                enviarPregunta(chatId);
            }else {
                enviarRespuesta(section,index,response,chatId,nombre);
                siguientepregunta(chatId,response,index);
            }
        } else{
            enviarRespuesta(section,index,response,chatId,nombre);
            siguientepregunta(chatId,response,index);
        }
    }

    private void enviarRespuesta(String seccion,Integer preguntaid, String response,Long telegramid,String nombreid) {
        serviceCuestionario cuestionarioService =new serviceCuestionario();
        userCuestionario cuestionario = new userCuestionario();


        cuestionario.setSeccion(seccion);
        cuestionario.setPreguntaid(preguntaid);
        cuestionario.setResponse(response);
        cuestionario.setTelegramid(telegramid);
        cuestionario.setNombreid(nombreid);


        try {
            cuestionarioService.crearUsuario(cuestionario);
            System.out.println("User created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void siguientepregunta(long chatId,String response,int index) {
        sendText(chatId, "Esta fue tu respuesta: " + response);
        indicePregunta.put(chatId, index + 1);
        enviarPregunta(chatId);
    }

    private String formatUserInfo(String firstName, String lastName) {
        return firstName + " " + lastName + " ";
    }


    private void processEmailInput(long chat_id, String email) {
        sendText(chat_id, "Se recibio tu correo: " + email);
        estadoConversacion.remove(chat_id); // Reset del estado
        try{
            usuarioConectado = userService.getUserByEmail(email);
        } catch (Exception e) {
            System.err.println("Error al obtener el usuario por correo: " + e.getMessage());
            e.printStackTrace();
        }


        if (usuarioConectado == null) {
            sendText(chat_id, "Su correo no se registro.");
        } else {
            usuarioConectado.setTelegramid(chat_id);
            try {
                userService.updateUser(usuarioConectado);
            } catch (Exception e) {
                System.err.println("Error al actualizar el usuario: " + e.getMessage());
                e.printStackTrace();
            }

            sendText(chat_id, "Usuario actualizado con éxito!");
        }
    }






    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public botCuestionario() {

        preguntas.put("SECTION_1", new String[]{"🤦‍♂️1.1- Estas aburrido?", "😂😂 1.2- Te bañaste hoy?", "🤡🤡 Pregunta 1.3"});
        preguntas.put("SECTION_2", new String[]{"Pregunta 2.1", "Pregunta 2.2", "Pregunta 2.3"});
        preguntas.put("SECTION_3", new String[]{"Pregunta 3.1", "Pregunta 3.2", "Pregunta 3.3"});
        preguntas.put("SECTION_4", new String[]{"4.1-Como te encuentras?","4.2-Que edad tienes","4.3- El programador se quedo sin ideas verdad?","4.4-Programador esta en decadencia?"});
    }






}
