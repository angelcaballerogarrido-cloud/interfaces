package com.example.newsfragments.parsers;

import android.os.Handler;
import android.os.Looper;

import com.example.newsfragments.models.NewsItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CsvParser {

    public interface OnNewsParsedListener {
        void onParsed(List<NewsItem> newsList);
    }

    public static void fetchAndParseCsv(String csvUrl, OnNewsParsedListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<NewsItem> list = new ArrayList<>();
            try {
                // Modificar la URL según el idioma del dispositivo (Requisito 2.b: cargar de otra pestaña de Google Sheets)
                String targetUrl = csvUrl;
                String lang = Locale.getDefault().getLanguage();
                if ("en".equals(lang)) {
                    targetUrl += "&gid=182739485"; // English Tab ID
                } else if ("fr".equals(lang)) {
                    targetUrl += "&gid=987654321"; // French Tab ID
                }

                URL url = new URL(targetUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

                String line;
                boolean isFirst = true;
                int index = 0;
                while ((line = reader.readLine()) != null) {
                    if (isFirst) { isFirst = false; continue; }
                    
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    
                    // Si el primer token está entre comillas y contiene comas,
                    // es muy probable que toda la fila esté guardada en una sola celda.
                    if (tokens.length > 0 && tokens[0].startsWith("\"") && tokens[0].endsWith("\"") && tokens[0].contains(",")) {
                        String extractedLine = tokens[0].substring(1, tokens[0].length() - 1);
                        tokens = extractedLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    }
                    
                    if (tokens.length >= 5) {
                        String title = tokens[0].replace("\"", "").trim();
                        String shortDesc = tokens[1].replace("\"", "").trim();
                        String thumb = tokens[2].replace("\"", "").trim();
                        String content = tokens[3].replace("\"", "").trim();
                        String largeImg = tokens[4].replace("\"", "").trim();
                        
                        // Parsear campos extra (Fecha e Importancia) si existen en el CSV, o autogenerar de forma coherente
                        String date = (tokens.length >= 6) ? tokens[5].replace("\"", "").trim() : "2026-05-" + (22 - (index % 10));
                        int importance = 3;
                        if (tokens.length >= 7) {
                            try {
                                importance = Integer.parseInt(tokens[6].replace("\"", "").trim());
                            } catch (NumberFormatException e) {
                                importance = 3;
                            }
                        } else {
                            importance = 5 - (index % 5); // 1 a 5
                        }
                        
                        NewsItem item = new NewsItem(title, shortDesc, thumb, content, largeImg, date, importance);
                        list.add(item);
                        index++;
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                list.addAll(getMockData());
            }

            handler.post(() -> listener.onParsed(list));
        });
    }

    public static List<NewsItem> getMockData() {
        List<NewsItem> mock = new ArrayList<>();
        String lang = Locale.getDefault().getLanguage();

        if ("en".equals(lang)) {
            // Mock Data en Inglés
            mock.add(new NewsItem(
                "Android 16: AI in the OS Core",
                "Google integrates native AI models into the operating system.",
                "https://picsum.photos/seed/android16/150/150",
                "Google has announced Android 16 with native artificial intelligence models integrated at the core kernel level of the operating system. These new on-device models can answer questions, summarize content, and provide assistance without any internet connection. Thermal management has also been significantly improved.",
                "https://picsum.photos/seed/android16/600/400",
                "2026-05-20", 5
            ));
            mock.add(new NewsItem(
                "Fragments: Modern Architecture",
                "How to use Fragments correctly in 2026.",
                "https://picsum.photos/seed/fragments/150/150",
                "Fragments are modular components of the Android user interface. They allow developers to create adaptive designs that show one or two panels depending on screen size. Communication between Fragments must always pass through the Activity or a shared ViewModel, never directly.",
                "https://picsum.photos/seed/fragments/600/400",
                "2026-05-18", 4
            ));
            mock.add(new NewsItem(
                "WorkManager: Future of Threading",
                "Deep dive into ExecutorService vs WorkManager.",
                "https://picsum.photos/seed/workmanager/150/150",
                "WorkManager is the recommended solution for background tasks that must be guaranteed to run even if the application process dies. AsyncTask was deprecated in Android 11 due to memory leak issues and a lack of lifecycle integration. ExecutorService is the direct alternative for immediate operations.",
                "https://picsum.photos/seed/workmanager/600/400",
                "2026-05-15", 3
            ));
            mock.add(new NewsItem(
                "Widgets in Android 12+",
                "Designing adaptive widgets with Material You.",
                "https://picsum.photos/seed/widgets/150/150",
                "App Widgets allow users to display key information directly on their home screen. In Android 12, Google introduced rounded corners and dynamic color capabilities, integrating them perfectly with the system-wide Material You theme on compatible devices.",
                "https://picsum.photos/seed/widgets/600/400",
                "2026-05-10", 2
            ));
            mock.add(new NewsItem(
                "Animations enhancing UX",
                "Advanced animation techniques in Android.",
                "https://picsum.photos/seed/animations/150/150",
                "Animations are not just decorations; they reduce the perceived waiting time and help the user comprehend transitions in the UI. Skeleton loading, shared element transitions, and staggered list entry animations are highly effective techniques.",
                "https://picsum.photos/seed/animations/600/400",
                "2026-05-12", 4
            ));
        } else if ("fr".equals(lang)) {
            // Mock Data en Francés
            mock.add(new NewsItem(
                "Android 16: L'IA au Cœur de l'OS",
                "Google intègre des modèles d'IA natifs dans le système.",
                "https://picsum.photos/seed/android16/150/150",
                "Google a annoncé Android 16 avec des modèles d'intelligence artificielle intégrés nativement au niveau du noyau du système d'exploitation. Les nouveaux modèles s'exécutent en local sur l'appareil et permettent de répondre aux questions, de résumer du contenu et de fournir de l'assistance sans connexion internet. La gestion thermique a également été grandement optimisée.",
                "https://picsum.photos/seed/android16/600/400",
                "2026-05-20", 5
            ));
            mock.add(new NewsItem(
                "Fragments: Architecture Moderne",
                "Comment utiliser correctement les Fragments en 2026.",
                "https://picsum.photos/seed/fragments/150/150",
                "Les Fragments sont des pièces modulaires de l'interface utilisateur d'Android. Ils permettent de concevoir des designs adaptatifs qui affichent un ou deux volets selon la taille de l'écran. La communication entre Fragments doit toujours transiter par l'Activité ou un ViewModel partagé, jamais en direct.",
                "https://picsum.photos/seed/fragments/600/400",
                "2026-05-18", 4
            ));
            mock.add(new NewsItem(
                "WorkManager: L'avenir du Threading",
                "Comparatif en profondeur entre ExecutorService et WorkManager.",
                "https://picsum.photos/seed/workmanager/150/150",
                "WorkManager est la solution recommandée pour les tâches en arrière-plan qui doivent être garanties, même si le processus de l'application s'arrête. AsyncTask a été déprécié dans Android 11 en raison de fuites de mémoire et de l'absence de contrôle du cycle de vie. ExecutorService est l'alternative directe pour des tâches immédiates.",
                "https://picsum.photos/seed/workmanager/600/400",
                "2026-05-15", 3
            ));
            mock.add(new NewsItem(
                "Widgets sous Android 12+",
                "Conception de widgets adaptatifs avec Material You.",
                "https://picsum.photos/seed/widgets/150/150",
                "Les Widgets d'application Android permettent d'afficher des informations directement sur l'écran d'accueil. Dans Android 12, Google a introduit les coins arrondis et le dynamisme des couleurs pour intégrer parfaitement les widgets au thème Material You.",
                "https://picsum.photos/seed/widgets/600/400",
                "2026-05-10", 2
            ));
            mock.add(new NewsItem(
                "Des Animations pour améliorer l'UX",
                "Techniques avancées d'animation sous Android.",
                "https://picsum.photos/seed/animations/150/150",
                "Les animations ne sont pas de simples décorations: elles réduisent la perception du temps d'attente et aident l'utilisateur à comprendre ce qui se passe. Le Skeleton Loading, les transitions d'éléments partagés et les animations d'entrée en cascade sont très efficaces.",
                "https://picsum.photos/seed/animations/600/400",
                "2026-05-12", 4
            ));
        } else {
            // Mock Data en Español (Fallback por defecto)
            mock.add(new NewsItem(
                "Android 16: IA en el núcleo del SO",
                "Google integra modelos de IA nativos en el sistema.",
                "https://picsum.photos/seed/android16/150/150",
                "Google ha anunciado Android 16 con modelos de inteligencia artificial integrados a nivel del kernel del sistema operativo. Los nuevos modelos on-device permiten responder preguntas, resumir contenido y proporcionar asistencia sin necesidad de conexión a internet. La gestión térmica también ha sido mejorada significativamente.",
                "https://picsum.photos/seed/android16/600/400",
                "2026-05-20", 5
            ));
            mock.add(new NewsItem(
                "Fragments: Arquitectura Moderna",
                "Cómo usar Fragments en 2026 correctamente.",
                "https://picsum.photos/seed/fragments/150/150",
                "Los Fragments son piezas modulares de la interfaz de usuario de Android. Permiten crear diseños adaptativos que muestran uno o dos paneles según el tamaño de la pantalla. La comunicación entre Fragments siempre debe pasar por la Activity o ViewModel compartido, nunca directamente entre ellos.",
                "https://picsum.photos/seed/fragments/600/400",
                "2026-05-18", 4
            ));
            mock.add(new NewsItem(
                "WorkManager: El futuro del threading",
                "ExecutorService vs WorkManager en profundidad.",
                "https://picsum.photos/seed/workmanager/150/150",
                "WorkManager es la solución recomendada para tareas en segundo plano que deben garantizarse incluso si el proceso muere. AsyncTask fue deprecado en Android 11 por problemas de fugas de memoria y falta de control del ciclo de vida. ExecutorService es la alternativa directa para tareas inmediatas.",
                "https://picsum.photos/seed/workmanager/600/400",
                "2026-05-15", 3
            ));
            mock.add(new NewsItem(
                "Widgets en Android 12+",
                "Diseño de widgets adaptativos con Material You.",
                "https://picsum.photos/seed/widgets/150/150",
                "Los App Widgets de Android permiten mostrar información de la app directamente en la pantalla de inicio. En Android 12, Google introdujo el concepto de Rounded Corners y Dynamic Colors para los widgets, integrándolos perfectamente con el sistema de Material You del dispositivo.",
                "https://picsum.photos/seed/widgets/600/400",
                "2026-05-10", 2
            ));
            mock.add(new NewsItem(
                "Animaciones que mejoran la UX",
                "Técnicas avanzadas de animación en Android.",
                "https://picsum.photos/seed/animations/150/150",
                "Las animaciones no son solo decoración: reducen la percepción de tiempo de espera y ayudan al usuario a entender qué está pasando en la interfaz. El Skeleton Loading, las transiciones compartidas de elementos y las animaciones de entrada en listas son las técnicas más efectivas.",
                "https://picsum.photos/seed/animations/600/400",
                "2026-05-12", 4
            ));
        }

        return mock;
    }
}

