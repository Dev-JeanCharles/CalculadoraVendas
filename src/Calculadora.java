import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Calculadora {
    private static final String ARQUIVO = "vendas.txt";
    private static final int META_INDIVIDUAL = 7;
    private static final int META_EQUIPE = 145;

    public static void main(String[] args) {
        TreeMap<String, Integer> acumulado = carregarVendasAcumuladas();
        TreeMap<String, Integer> vendasDoDia = new TreeMap<>();

        Scanner sc = new Scanner(System.in);

        System.out.println("=== Cadastro de Vendas ===");

        for (String atendente : acumulado.keySet()) {
            System.out.print("Digite a quantidade de vendas de " + atendente + " hoje: ");
            int vendasHoje = Integer.parseInt(sc.nextLine());

            int totalVendas = acumulado.getOrDefault(atendente, 0) + vendasHoje;
            acumulado.put(atendente, totalVendas);

            vendasDoDia.put(atendente, vendasHoje);
        }
        sc.close();

        salvarVendas(acumulado);
        gerarRelatorio(vendasDoDia);

    }

    private static TreeMap<String, Integer> carregarVendasAcumuladas() {
        TreeMap<String, Integer> vendas = new TreeMap<>();
        File file = new File(ARQUIVO);

        if (!file.exists()) {
            vendas.put("Jean", 0);
            vendas.put("Augusto", 0);
            vendas.put("Ianca", 0);
            vendas.put("Liana", 0);
            vendas.put("Raysa", 0);
            vendas.put("Thaysa", 0);
            vendas.put("Larissa", 0);
            return vendas;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length == 2) {
                    String nome = partes[0];
                    int quantidade = Integer.parseInt(partes[1]);
                    vendas.put(nome, quantidade);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar arquivo de vendas: " + e.getMessage());
        }

        return vendas;
    }

    private static void salvarVendas(TreeMap<String, Integer> vendas) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARQUIVO))) {
            for (Map.Entry<String, Integer> entry : vendas.entrySet()) {
                bw.write(entry.getKey() + ";" + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar arquivo de vendas: " + e.getMessage());
        }
    }

    private static void gerarRelatorio(TreeMap<String, Integer> vendasDoDia) {
        int totalEquipe = 0;

        System.out.println("\n=== Relatório de Vendas DO DIA ===");

        for (Map.Entry<String, Integer> entry : vendasDoDia.entrySet()) {
            String nome = entry.getKey();
            int vendasFeitas = entry.getValue();
            int vendasFaltando = META_INDIVIDUAL - vendasFeitas;
            int valorReais = vendasFeitas * 145;

            String statusMeta  = vendasFaltando <= 0
                    ? "\uD83C\uDFAF Meta batida"
                    : "Faltam " + vendasFaltando + " vendas";

            System.out.printf("%-8s: %2d vendas | R$ %-6d | %s\n",
                    nome, vendasFeitas, valorReais, statusMeta);

            totalEquipe += vendasFeitas;
        }

        int faltandoVendas = META_EQUIPE - totalEquipe;

        if (faltandoVendas <= 0) {
            System.out.println("🎉 Parabéns! A equipe bateu a meta de 145 vendas no dia!");
        } else {
            System.out.println("FALTAM " + faltandoVendas + " vendas para a equipe atingir a meta diária de 145.");
        }

        System.out.println("--------------------------------------");
        System.out.println("TOTAL DA EQUIPE (hoje): " + totalEquipe + " vendas");
        System.out.println("META INDIVIDUAL: 7 vendas ou R$ 1015,00");
        System.out.println("META EQUIPE (diária ou até dia 31): 145 vendas\n");
    }
}
