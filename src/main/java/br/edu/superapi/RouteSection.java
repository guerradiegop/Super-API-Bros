package br.edu.superapi;

import java.awt.Rectangle;
import java.util.*;

/** Seeded route motifs, constrained by the normal jump rather than special powers. */
public record RouteSection(int shape, java.util.List<Rectangle> ledges, java.util.List<Rectangle> coins, Rectangle gap) {
    private static final int[][] HEIGHTS = {
        {410,335,270}, {395,395,395}, {405,330,405},
        {400,345,345,400}, {410,375,340,375,410}, {410,350,290,350}
    };
    public static java.util.List<RouteSection> generate(Random random, int region) {
        java.util.List<RouteSection> routes = new ArrayList<>();
        java.util.List<Integer> bag = new ArrayList<>();
        int previous = -1, cursor = 0;
        int[] spacingExtras = new int[4];
        for (int block = 0; block < GameModel.BLOCK_COUNT; block++) {
            if (block % 5 == 0) {
                cursor = (block < 5 ? 0 : GameModel.TERMINAL_X) + 140 + random.nextInt(41);
                int remaining = 80 + random.nextInt(81);
                for (int i=0; i<3; i++) { spacingExtras[i]=random.nextInt(remaining+1); remaining-=spacingExtras[i]; }
                spacingExtras[3]=remaining;
                for (int i=3;i>0;i--) { int j=random.nextInt(i+1), swap=spacingExtras[i]; spacingExtras[i]=spacingExtras[j]; spacingExtras[j]=swap; }
            } else cursor += 620 + spacingExtras[block % 5 - 1];
            if (bag.isEmpty()) { for (int i = 0; i < HEIGHTS.length; i++) bag.add(i); Collections.shuffle(bag, random); }
            int index = bag.size()-1;
            if (bag.get(index) == previous && index > 0) index--;
            int shape = bag.remove(index); previous = shape;
            int[] heights = HEIGHTS[shape];
            int start = cursor;
            int x = start, shift = random.nextInt(17)-8;
            java.util.List<Rectangle> ledges = new ArrayList<>(), coins = new ArrayList<>();
            for (int step = 0; step < heights.length; step++) {
                int spacing = heights.length == 3 ? 120 + random.nextInt(16) : heights.length == 4 ? 85 + random.nextInt(11) : 70 + random.nextInt(5);
                if (step > 0) x += spacing;
                int width = heights.length == 5 ? 60 + random.nextInt(11) : heights.length == 4 ? 78 + random.nextInt(12) : 88 + random.nextInt(21);
                int y = heights[step] + shift;
                boolean hasCoin = step == 0 || step == heights.length / 2 || step == heights.length-1;
                Rectangle coin = new Rectangle(x + width/2 - 9, y - 32, 18, 22);
                boolean middle = step > 0 && step < heights.length-1;
                boolean movingRegion = region == 5 || region == 6 || region == 8;
                boolean fragileRegion = region == 2 || region == 7 || region == 9;
                int roll = random.nextInt(5);
                boolean moving = middle && (movingRegion ? roll < 4 : !fragileRegion && roll == 0);
                boolean fragile = middle && !moving && (fragileRegion ? roll < 4 : roll == 1);
                // Guarantee the defining mechanism in snow/ruins without fixing its placement elsewhere.
                if (block == 0 && step == 1 && movingRegion) { moving = true; fragile = false; }
                if (block == 0 && step == 1 && fragileRegion) { fragile = true; moving = false; }
                Rectangle ledge = moving || fragile ? new TerrainPlatform(x,y,width,
                        moving ? TerrainPlatform.Kind.ICE_LIFT : TerrainPlatform.Kind.CRUMBLING, coin,
                        10 + random.nextInt(7), .018 + random.nextDouble()*.012, random.nextDouble()*Math.PI*2)
                        : new Rectangle(x,y,width,22);
                ledges.add(ledge); if (hasCoin) coins.add(coin);
            }
            Rectangle gap = new Rectangle(start + 425 + random.nextInt(11), GameModel.FLOOR, 65 + random.nextInt(31), 100);
            routes.add(new RouteSection(shape, java.util.List.copyOf(ledges), java.util.List.copyOf(coins), gap));
        }
        return java.util.List.copyOf(routes);
    }
}
