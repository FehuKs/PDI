package src.desafio;

import src.core.Images;
import src.core.AnalysisResult;

import src.filtros.FiltrodaMediana;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;


public class ContadorObjetos {

    private static final int AREA_MINIMA = 500;

    private final FiltrodaMediana filtroMediana;

    public ContadorObjetos(FiltrodaMediana filtroMediana) {
        this.filtroMediana = filtroMediana;
    }

    // -----------------------------------------------------------------------
    // Método principal
    // -----------------------------------------------------------------------
    public String contar(BufferedImage image) { return evaluate(image).summary(); }

    public AnalysisResult evaluate(BufferedImage image) {
        Images.require(image);
        return contarInternal(image);
    }

    private AnalysisResult contarInternal(BufferedImage image) {
        // 1. Mediana para remover ruído antes da segmentação por cor
        BufferedImage suavizada = filtroMediana.aplicarFiltroMediana(image);

        int w = suavizada.getWidth();
        int h = suavizada.getHeight();

        // 2. Classifica cada pixel: 0=Vermelho,1=Amarelo,2=Verde,3=Azul,-1=nenhum
        int[][] corPixel = classificarPixels(suavizada, w, h);

        boolean[][] visitado = new boolean[h][w];

        // Contadores de forma × cor  [cor][forma]  0=circ,1=quad,2=ret,3=tri
        int[][] contagem = new int[4][4];
        String[] nomeCor   = {"vermelhos", "amarelos", "verdes", "azuis"};
        String[] nomeForma = {"círculos", "quadrados", "retângulos", "triângulos"};

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int cor = corPixel[y][x];
                if (cor >= 0 && !visitado[y][x]) {
                    // FloodFill retorna: {x0,y0,x1,y1,area,perim}
                    int[] info = floodFillInfo(corPixel, visitado, x, y, w, h, cor);
                    int area = info[4];
                    if (area >= AREA_MINIMA) {
                        int forma = classificarForma(info);
                        contagem[cor][forma]++;
                    }
                }
            }
        }

        // Monta resultado
        StringBuilder sb = new StringBuilder();
        java.util.Map<String, String> counts = new java.util.LinkedHashMap<>();
        for (int c = 0; c < 4; c++) {
            for (int f = 0; f < 4; f++) {
                if (contagem[c][f] > 0) {
                    counts.put(nomeCor[c] + "." + nomeForma[f], String.valueOf(contagem[c][f]));
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(contagem[c][f]).append(" ").append(nomeForma[f]).append(" ").append(nomeCor[c]);
                }
            }
        }
        return new AnalysisResult(sb.length() > 0 ? AnalysisResult.Status.IDENTIFIED : AnalysisResult.Status.NOT_FOUND,
                sb.length() > 0 ? sb.toString() : "Nenhum objeto colorido encontrado", counts);
    }

    // -----------------------------------------------------------------------
    // Classificação de forma por descritores morfométricos
    //
    //  Circularidade  C  =  4π × área / perímetro²   ∈ (0, 1]
    //      C ≈ 1.0  → círculo
    //      C ≈ 0.78 → quadrado  (π/4)
    //      C < 0.6  → forma alongada (retângulo) ou triangular
    //
    //  Razão de aspecto  A  =  largura / altura  (sempre ≥ 1)
    //      A ≈ 1.0  → quadrado ou círculo
    //      A > 1.3  → retângulo deitado ou triângulo
    //
    //  Solidez  S  =  área / área_bbox
    //      S > 0.80 → forma "cheia" (círculo, quadrado, retângulo)
    //      S < 0.65 → triângulo (metade da bbox)
    //
    //  Lógica final:
    //   S < 0.65                      → TRIÂNGULO
    //   C > 0.82 && A < 1.2           → CÍRCULO
    //   C > 0.70 && A < 1.3           → QUADRADO
    //   caso contrário                → RETÂNGULO
    // -----------------------------------------------------------------------
    private int classificarForma(int[] info) {
        int x0   = info[0], y0 = info[1], x1 = info[2], y1 = info[3];
        int area = info[4];
        long perim = info[5];   // perímetro estimado pelo FloodFill

        int bw = x1 - x0 + 1;
        int bh = y1 - y0 + 1;

        double aspecto = (bw >= bh)
                ? (double) bw / Math.max(bh, 1)
                : (double) bh / Math.max(bw, 1);   // sempre ≥ 1

        double areaBbox   = (double) bw * bh;
        double solidez    = area / areaBbox;

        // Circularidade (protegida contra perímetro=0)
        double circ = (perim > 0)
                ? (4.0 * Math.PI * area) / ((double) perim * perim)
                : 0;

        // Limita a 1.0 (arredondamento do perímetro pode ultrapassar levemente)
        circ = Math.min(circ, 1.0);

        if (solidez < 0.65)              return 3; // Triângulo
        if (solidez < 0.90 && circ > 0.82 && aspecto < 1.25) return 0; // Círculo
        if (circ  > 0.68 && aspecto < 1.35) return 1; // Quadrado
        return 2;                                   // Retângulo
    }

    // -----------------------------------------------------------------------
    // Classificação HSV de pixel
    // -----------------------------------------------------------------------
    private int[][] classificarPixels(BufferedImage img, int w, int h) {
        int[][] mapa = new int[h][w];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                mapa[y][x] = classificarCor(img.getRGB(x, y));
        return mapa;
    }

    private int classificarCor(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >>  8) & 0xFF) / 255f;
        float b  =  (rgb       & 0xFF) / 255f;

        float max   = Math.max(r, Math.max(g, b));
        float min   = Math.min(r, Math.min(g, b));
        float delta = max - min;

        // CORREÇÃO: limiar reduzido de 0.25→0.20 e delta de 0.15→0.12
        // para capturar cores mais claras sem perder objetos
        if (max < 0.20f || delta < 0.12f) return -1;

        float h = 0;
        if (delta > 0) {
            if      (max == r) h = 60f * (((g - b) / delta) % 6);
            else if (max == g) h = 60f * ((b - r) / delta + 2);
            else               h = 60f * ((r - g) / delta + 4);
            if (h < 0) h += 360;
        }

        // CORREÇÃO: saturação mínima reduzida de 0.35→0.25
        if ((delta / max) < 0.25f) return -1;

        if (h < 30  || h >= 330) return 0; // Vermelho
        if (h >= 30 && h < 75)   return 1; // Amarelo
        if (h >= 75 && h < 165)  return 2; // Verde
        if (h >= 165 && h < 270) return 3; // Azul
        return -1;
    }

    // -----------------------------------------------------------------------
    // FloodFill BFS 4-direções — retorna {x0,y0,x1,y1,area,perim}
    //
    // CORREÇÃO: agora rastreia a bounding box e estima o perímetro (contando
    // pixels de objeto que têm pelo menos 1 vizinho 4-conn de fundo), o que
    // permite calcular a circularidade na classificação de forma.
    // -----------------------------------------------------------------------
    private int[] floodFillInfo(int[][] cor, boolean[][] vis,
                                 int sx, int sy, int w, int h, int c) {
        Queue<int[]> fila = new LinkedList<>();
        fila.add(new int[]{sx, sy});
        vis[sy][sx] = true;
        int x0 = sx, y0 = sy, x1 = sx, y1 = sy, area = 0, perim = 0;
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        while (!fila.isEmpty()) {
            int[] p = fila.poll();
            area++;
            x0 = Math.min(x0, p[0]); y0 = Math.min(y0, p[1]);
            x1 = Math.max(x1, p[0]); y1 = Math.max(y1, p[1]);
            boolean bordeiro = false;
            for (int d = 0; d < 4; d++) {
                int nx = p[0] + dx[d];
                int ny = p[1] + dy[d];
                if (nx < 0 || nx >= w || ny < 0 || ny >= h) {
                    bordeiro = true;
                    continue;
                }
                if (cor[ny][nx] != c) {
                    bordeiro = true; // pixel vizinho é fundo → este pixel está na borda
                } else if (!vis[ny][nx]) {
                    vis[ny][nx] = true;
                    fila.add(new int[]{nx, ny});
                }
            }
            if (bordeiro) perim++;
        }
        return new int[]{x0, y0, x1, y1, area, perim};
    }
}
