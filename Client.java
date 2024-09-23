import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("127.0.0.1", 3000);
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                Scanner scanner = new Scanner(System.in);
        ) {
            // Sunucudan hoşgeldin mesajını ve ID bilgisini alıyoruz
            String welcomeMessage = reader.readLine();
            System.out.println(welcomeMessage);

            // Sunucudan gelen mesajları dinleyen iş parçacığı
            Thread receiveMessages = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = reader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Sunucuyla bağlantı kesildi.");
                }
            });

            receiveMessages.start();

            // Kullanıcıdan mesaj alıp sunucuya gönderme
            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    writer.println(message);
                    break;
                }
                writer.println(message);
            }

            // Bağlantıyı kapatıyoruz
            socket.close();
            System.out.println("Bağlantı sonlandırıldı.");

        } catch (IOException e) {
            System.out.println("Sunucuya bağlanırken hata oluştu: " + e.getMessage());
        }
    }
}

