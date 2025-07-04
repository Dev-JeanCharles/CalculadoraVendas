import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class CalculadoraGUI {
    private static final String ARQUIVO = "vendas.txt";
    private static final int META_INDIVIDUAL = 7;
    private static final int META_EQUIPE = 145;

    private static final String[] ATENDENTES = {
            "Jean", "Augusto", "Ianca", "Liana", "Raysa", "Thaysa", "Larissa"
    };

    private static final TreeMap<String, JTextField> campos = new TreeMap<>();


    public static void main(String[] args) {
        SwingUtilities.invokeLater(CalculadoraGUI::criarInterface);
    }

    private static void criarInterface() {
        JFrame frame = new JFrame("Cadastro de Vendas do Dia");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel painel = new JPanel(new GridLayout(ATENDENTES.length, 2, 5, 5));

        for (String nome : ATENDENTES) {
            JLabel label = new JLabel("Vendas de " + nome + ":");
            JTextField campo = new JTextField("0");
            campos.put(nome, campo);
            painel.add(label);
            painel.add(campo);
        }

        JButton botaoSalvar = new JButton("Salvar e Gerar Relatório");
        botaoSalvar.addActionListener(e -> processarVendas());

        JButton botaoSair= new JButton("Sair");
        botaoSair.addActionListener(e -> System.exit(0));

        JPanel painelBotoes = new JPanel();
        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoSair);

        frame.add(painel, BorderLayout.CENTER);
        frame.add(painelBotoes, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void processarVendas() {
        TreeMap<String, Integer> acumulado = carregarVendasAcumuladas();
        TreeMap<String, Integer> vendasDoDia = new TreeMap<>();

        try {
            for (String nome : ATENDENTES) {
                int vendasHoje = Integer.parseInt(campos.get(nome).getText().trim());

                int totalVendas = acumulado.getOrDefault(nome, 0) + vendasHoje;
                acumulado.put(nome, totalVendas);

                vendasDoDia.put(nome, vendasHoje); // vendas só do dia
            }

            salvarVendas(acumulado);
            gerarRelatorio(vendasDoDia);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Erro: Digite apenas números válidos para as vendas.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static TreeMap<String, Integer> carregarVendasAcumuladas() {
        TreeMap<String, Integer> vendas = new TreeMap<>();
        File file = new File(ARQUIVO);

        if (!file.exists()) {
            for (String nome : ATENDENTES) {
                vendas.put(nome, 0);
            }
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
            JOptionPane.showMessageDialog(null, "Erro ao carregar arquivo: " + e.getMessage());
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
            JOptionPane.showMessageDialog(null, "Erro ao salvar vendas: " + e.getMessage());
        }
    }

    private static void gerarRelatorio(TreeMap<String, Integer> vendasDoDia) {
        int totalEquipe = 0;
        StringBuilder relatorio = new StringBuilder("=== RELATÓRIO DE VENDAS DO DIA ===\n\n");

        for (Map.Entry<String, Integer> entry : vendasDoDia.entrySet()) {
            String nome = entry.getKey();
            int vendasFeitas = entry.getValue();
            int vendasFaltando = META_INDIVIDUAL - vendasFeitas;
            int valorReais = vendasFeitas * 145;

            String statusMeta  = vendasFaltando <= 0
                    ? "\uD83C\uDFAF Meta batida"
                    : "Faltam " + vendasFaltando + " vendas";

            relatorio.append(String.format("%-8s: %2d vendas | R$ %-6d | %s\n",
                    nome, vendasFeitas, valorReais, statusMeta));

            totalEquipe += vendasFeitas;
        }

        int faltandoVendas = META_EQUIPE - totalEquipe;

        relatorio.append("\n--------------------------------------\n");

        if (faltandoVendas <= 0) {
            relatorio.append("🎉 Parabéns! A equipe bateu a meta de 145 vendas no dia!\n");
        } else {
            relatorio.append("FALTAM " + faltandoVendas + " vendas para a equipe atingir a meta diária de 145.\n");
        }

        relatorio.append("TOTAL DA EQUIPE (hoje): ").append(totalEquipe).append(" vendas\n");
        relatorio.append("META INDIVIDUAL: 7 vendas ou R$ 1015,00\n");
        relatorio.append("META EQUIPE (diária ou até dia 31): 145 vendas");

        JTextArea area = new JTextArea(relatorio.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(500, 350));

        JOptionPane.showMessageDialog(null, scrollPane, "Relatório do Dia", JOptionPane.INFORMATION_MESSAGE);
    }
}
