package ex1llm;

/**
 *
 * @author Slam
 */
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ImprovedLanguageModel implements Serializable {

    private Map<String, Map<String, Integer>> nGramCounts = new HashMap<>();
    private int n; // Tamaño de n-gramas
    private int version = 1;

    public ImprovedLanguageModel(int n) {
        this.n = n;
    }

    // Método para entrenar el modelo
    public void train(List<String> sentences) {
        for (String sentence : sentences) {
            String[] tokens = sentence.split(" ");
            List<String> validTokens = new ArrayList<>();
            for (String token : tokens) {
                if (!token.trim().isEmpty() && token.matches("[\\w\\p{Punct}]+")) {
                    validTokens.add(token);
                }
            }
            tokens = validTokens.toArray(new String[0]);

            for (int i = 0; i < tokens.length - n + 1; i++) {
                String[] nGram = Arrays.copyOfRange(tokens, i, i + n);
                String context = String.join(" ", Arrays.copyOfRange(nGram, 0, n - 1));
                String nextToken = nGram[n - 1];

                // Aquí fusionamos los conteos existentes con los nuevos
                nGramCounts.putIfAbsent(context, new HashMap<>());
                nGramCounts.get(context).put(nextToken, nGramCounts.get(context).getOrDefault(nextToken, 0) + 1);
            }
        }
        System.out.println("Total n-grams generados: " + nGramCounts.size());
    }

    // Método para generar texto
    public String generate(String seed, int length, double temperature) {
        if (!nGramCounts.containsKey(seed)) {
            // Seleccionar un contexto aleatorio del modelo
            List<String> keys = new ArrayList<>(nGramCounts.keySet());
            seed = keys.get(new Random().nextInt(keys.size()));
            System.out.println("No se encontró el contexto, utilizando un contexto aleatorio: " + seed);
        }

        StringBuilder generatedText = new StringBuilder(seed);
        String[] seedTokens = seed.split(" ");

        // Comenzar con todo el input como contexto
        String context = String.join(" ", Arrays.copyOfRange(seedTokens, Math.max(0, seedTokens.length - (n - 1)), seedTokens.length));

        for (int i = 0; i < length; i++) {
            Map<String, Integer> nextTokenCounts = nGramCounts.get(context);
            if (nextTokenCounts == null || nextTokenCounts.isEmpty()) {
                break; // Detener si no hay más tokens para el contexto actual
            }

            String nextToken = chooseNextToken(nextTokenCounts, temperature);
            generatedText.append(" ").append(nextToken);

            // Actualizar contexto dinámicamente
            context = updateContext(context, nextToken);
        }
        return generatedText.toString();
    }

    // Método para actualizar el contexto (sin limitarlo a n-1 tokens)
    private String updateContext(String context, String nextToken) {
        String[] tokens = context.split(" ");
        // Mantener sólo las últimas (n-1) palabras del contexto
        if (tokens.length >= (n - 1)) {
            return String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length)) + " " + nextToken;
        }
        return String.join(" ", context, nextToken);
    }

    // Método para elegir el siguiente token
    private String chooseNextToken(Map<String, Integer> counts, double temperature) {
        if (temperature <= 0) {
            throw new IllegalArgumentException("Temperature must be greater than 0.");
        }
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        double[] probabilities = new double[counts.size()];
        String[] tokens = new String[counts.size()];

        int index = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            tokens[index] = entry.getKey();
            double probability = (double) entry.getValue() / total;
            probabilities[index] = Math.pow(probability, 1.0 / temperature);
            index++;
        }

        double sum = Arrays.stream(probabilities).sum();
        if (sum == 0) {
            return tokens[tokens.length - 1]; // Evita dividir entre 0
        }
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= sum;
        }

        double rand = Math.random();
        for (int i = 0; i < probabilities.length; i++) {
            rand -= probabilities[i];
            if (rand <= 0) {
                return tokens[i];
            }
        }
        return tokens[tokens.length - 1]; // Si no se selecciona ningún token, devolver el último
    }

    // Método para guardar el modelo
    public void saveModel(String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            System.out.println("El archivo ya existe. ¿Deseas sobrescribirlo? (y/n)");
            Scanner sc = new Scanner(System.in);
            String response = sc.nextLine();
            if (!response.equalsIgnoreCase("y")) {
                System.out.println("Guardado cancelado.");
                return;
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(filename))))) {
            // Añadir un número de versión al archivo
            oos.writeInt(version); // Versión 1
            oos.writeObject(nGramCounts);
            System.out.println("Modelo guardado exitosamente con versión: " + version + ".");
            System.out.println("Datos guardados: " + nGramCounts.size() + " tokens.");
        }

    }

    // Método para cargar el modelo y fusionarlo con el actual
    public void loadModel(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new GZIPInputStream(new FileInputStream(filename))))) {
            version = ois.readInt(); // Leer la versión
            if (version == 1) {
                Map<String, Map<String, Integer>> loadedNGramCounts = (Map<String, Map<String, Integer>>) ois.readObject();

                // Fusionar los n-gramas cargados con los existentes
                for (Map.Entry<String, Map<String, Integer>> entry : loadedNGramCounts.entrySet()) {
                    String context = entry.getKey();
                    Map<String, Integer> loadedNextTokens = entry.getValue();

                    // Si ya existe el contexto, sumar los contadores de tokens
                    nGramCounts.putIfAbsent(context, new HashMap<>());
                    for (Map.Entry<String, Integer> tokenEntry : loadedNextTokens.entrySet()) {
                        String token = tokenEntry.getKey();
                        int count = tokenEntry.getValue();
                        nGramCounts.get(context).put(token, nGramCounts.get(context).getOrDefault(token, 0) + count);
                    }
                }
                System.out.println("Datos cargados: " + nGramCounts.size() + " tokens.\n Versión " + version);
            } else {
                System.err.println("Versión de modelo no compatible");
            }
        }
    }

    // obtener los nGrams
    public int getnGramCounts() {
        return nGramCounts.size();
    }
}
