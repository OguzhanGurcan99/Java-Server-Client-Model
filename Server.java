import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    // İstemcileri ID'leriyle eşleştirmek için ConcurrentHashMap kullanıyoruz
    private static ConcurrentHashMap<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private static AtomicInteger clientIdCounter = new AtomicInteger(1); // ID atamak için sayaç

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(3000)) {
            System.out.println("Sunucu başlatıldı. Bağlantı bekleniyor...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientIdCounter.getAndIncrement(); // Yeni istemciye ID ataması
                System.out.println("Yeni bir istemci bağlandı. ID: " + clientId);

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId);
                clients.put(clientId, clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mesajı tüm istemcilere yayınlayan metot
    public static void broadcastMessage(String message, int senderId) {
        clients.forEach((clientId, clientHandler) -> {
            if (clientId != senderId) {
                clientHandler.sendMessage("Client " + senderId + ": " + message);
            }
        });
    }

    // İstemci bağlantısı kapandığında çağrılır
    public static void removeClient(int clientId) {
        clients.remove(clientId);
        System.out.println("İstemci bağlantısı kesildi. ID: " + clientId);
    }

    // ClientHandler sınıfı, her istemci için ayrı bir iş parçacığı oluşturur
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private int clientId;
        private PrintWriter writer;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try (
                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    OutputStream output = socket.getOutputStream();
            ) {
                writer = new PrintWriter(output, true);

                // İstemciye kendi ID bilgisini gönderiyoruz
                writer.println("Hoşgeldiniz! ID'niz: " + clientId);

                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Client " + clientId + ": " + message);
                    broadcastMessage(message, clientId);
                }
            } catch (IOException e) {
                System.out.println("İstemciyle bağlantıda hata oluştu. ID: " + clientId);
            } finally {
                removeClient(clientId);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Mesajı istemciye gönderir
        public void sendMessage(String message) {
            writer.println(message);
        }
    }
}
