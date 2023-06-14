import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Clase servidor
 * Necesaria para iniciar el código del juego
 */
public class server implements Runnable{
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    /**
     * Creación de la lista deconecciones
     */
    public server(){
        done = false;
        connections = new ArrayList<>();
    }

    /**
     * Se ejecuta para abrir el socket e indicar que se estableció la conección
     */
    @Override
    public void run(){
        System.out.println("Si funciona :D");
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done){
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);

            }
        }catch (Exception e){
            shutdown();
        }

    }

    /**
     * Envía parametros
     * @param message
     */
    public void broadcast(String message){
        for(ConnectionHandler ch : connections){
            if (ch != null){
                ch.sendMessage(message);
            }
        }
    }

    /**
     * Cierra el socket
     */
    public void shutdown(){
        try{
            done = true;
            if(!server.isClosed()) {
                server.close();
            }
            for(ConnectionHandler ch : connections){
                ch.shutdown();
            }
        }catch(IOException e){
            //ignore
        }
    }

    /**
     * Listener para las indicaciones enviadas desde la otra aplicación
     */
    class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        public ConnectionHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run(){
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String message;
                //Aquí es donde se reciben los mensajes
                while((message = in.readLine())!= null){
                    if (message.equals("red")){
                        System.out.println("RED");
                        broadcast("Red!");
                    }else if (message.equals("blue")){
                        System.out.println("BLUE");
                        broadcast("Blue!");
                    }else if(message.equals("off")){
                        broadcast("off");
                        shutdown();
                    }
                }
            }catch(IOException e){
                shutdown();
            }
        }

        /**
         * Envía parametros
         * @param message
         */
        public void sendMessage(String message){
            out.println(message);
        }
        public void shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            }catch(IOException e){
                //ignore
            }
        }
    }

    /**
     * Ejecuta la aplicación
     * @param args
     */
    public static void main(String[] args) {
        server server = new server();
        server.run();
    }
}

