# Referências de dígitos

`digit-templates.properties` contém 40 máscaras binárias renderizadas de fontes: dígitos 0–9, famílias lógicas SansSerif/Serif e estilos regular/negrito, com fonte de 160 pontos. A captura foi feita no ambiente Linux com JDK 21, normalizada para 32×32 pixels e margem de seis pixels, usando o mesmo pipeline de geração dos templates.

Não foram usados pixels dos exemplos ou das imagens da professora para gerar essas máscaras. Nenhum arquivo de fonte é distribuído. As referências complementam os templates gerados das fontes locais, para preservar estilos conhecidos quando a resolução das fontes lógicas muda entre plataformas.

Cada propriedade `glyph.<dígito>.<variante>` contém um `BitSet` Java serializado em hexadecimal, em ordem de linhas: o bit de índice `y * size + x` indica primeiro plano. `size` e `margin` são conferidos ao carregar. As referências são recursos do JAR, disponíveis também fora do Maven.
