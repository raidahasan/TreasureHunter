/**
 * The Town Class is where it all happens.
 * The Town is designed to manage all the things a Hunter can do in town.
 * This code has been adapted from Ivan Turner's original program -- thank you Mr. Turner!
 */

public class Town {
    // instance variables
    private Hunter hunter;
    private Shop shop;
    private Terrain terrain;
    private String printMessage;
    private boolean toughTown;
    private String treasure = "";
    private static String[] treasures = new String[3];
    private boolean searched;
    private boolean dug;
    private String mode;
    private static boolean jungle;
    /**
     * The Town Constructor takes in a shop and the surrounding terrain, but leaves the hunter as null until one arrives.
     *
     * @param shop The town's shoppe.
     * @param toughness The surrounding terrain.
     */
    public Town(Shop shop, double toughness, String mode) {
        this.shop = shop;
        jungle = false;
        this.terrain = getNewTerrain();

        // the hunter gets set using the hunterArrives method, which
        // gets called from a client class
        hunter = null;
        printMessage = "";
        // higher toughness = more likely to be a tough town
        toughTown = (Math.random() < toughness);
        treasure = treasure();
        searched = false;
        dug = false;
        this.mode = mode;
    }

    public String getLatestNews() {
        return printMessage;
    }
    public static String[] getTreasures(){return treasures;}
    public static boolean getJungle(){return jungle;}
    public void resetTreasures(){
        treasures = new String[3];
    }

    /**
     * Assigns an object to the Hunter in town.
     *
     * @param hunter The arriving Hunter.
     */
    public void hunterArrives(Hunter hunter) {
        this.hunter = hunter;
        printMessage = "Welcome to town, " + hunter.getHunterName() + ".";

        if (toughTown) {
            printMessage += "\nIt's pretty rough around here, so watch yourself.";
        } else {
            printMessage += "\nWe're just a sleepy little town with mild mannered folk.";
        }
        printMessage += "\n";
    }
    public String treasure(){
        String treasure = "";
        int ran = (int) (Math.random()*4)+1;
        if(ran==1){
            treasure = "a crown";
        }else if(ran == 2){
            treasure = "a trophy";
        }else if(ran == 3){
            treasure = "a gem";
        }else{
            treasure = "dust";
        }
        return treasure;
    }

    /**
     * Handles the action of the Hunter leaving the town.
     *
     * @return true if the Hunter was able to leave town.
     */
    public boolean leaveTown() {
        boolean canLeaveTown = terrain.canCrossTerrain(hunter);
        if(hunter.hasItemInKit("sword") && canLeaveTown && jungle){
            printMessage = "You used your sword to cross the " + terrain.getTerrainName() + ".";
        }else if (canLeaveTown) {
            String item = terrain.getNeededItem();
            printMessage = "You used your " + item + " to cross the " + terrain.getTerrainName() + ".";
            if (checkItemBreak() && !mode.equals("easy")) {
                hunter.removeItemFromKit(item);
                printMessage += "\nUnfortunately, you lost your " + item + ".";
        }
            return true;
        }else {
            printMessage = "You can't leave town, " + hunter.getHunterName() + ". You don't have a " + terrain.getNeededItem() + ".";
            return false;
        }
        return canLeaveTown;
    }

    /**
     * Handles calling the enter method on shop whenever the user wants to access the shop.
     *
     * @param choice If the user wants to buy or sell items at the shop.
     */
    public void enterShop(String choice) {
        shop.enter(hunter, choice);
        System.out.println("You left the shop");
        printMessage = "";
    }

    public void digForTreasure(){
        if(hunter.hasItemInKit("shovel") && !dug) {
            if (Math.random() > .5) {
                int coins = (int) (Math.random() * 20) + 1;
                System.out.println("You dug up " + coins + " gold!");
                hunter.changeGold(coins);
            } else {
                System.out.println("You dug but only found dirt");
            }
            dug = true;
        }else if(dug){
            System.out.println("You already dug in this town");
        }else{
            System.out.println("You can't dig for gold without a shovel");
        }
    }
    public void huntForTreasure() {
        if(!searched) {
            boolean found = false;
            for (String s : treasures) {
                if (s != null && s.equals(treasure)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                System.out.println("You already have that");
            }else if (treasure.equals("dust")){
                System.out.println("You found dust :(");
            } else {
                System.out.println("You found " + treasure + "!");
                for (int i = 0; i < treasures.length; i++) {
                    if (treasures[i] == null) {
                        treasures[i]=treasure;
                        break;
                    }
                }
                if(treasures[2]!=null){
                    TreasureHunter.setNotOverToFalse();
                }
            }
            searched = true;
        }else{
            System.out.println("This town has already been searched.");
        }
    }

    /**
     * Gives the hunter a chance to fight for some gold.<p>
     * The chances of finding a fight and winning the gold are based on the toughness of the town.<p>
     * The tougher the town, the easier it is to find a fight, and the harder it is to win one.
     */
    public void lookForTrouble() {
        double noTroubleChance;
        if (toughTown && mode.equals("hard")) {
            noTroubleChance = 0.88;
        } else if (!toughTown && mode.equals("easy")) {
            noTroubleChance = 0.16;
        } else {
            noTroubleChance = 0.33;
        }

        if(TreasureHunter.secretMode() && hunter.hasItemInKit("sword")){
            printMessage = Colors.BLACK + "The brawler sees your sword, drops his wallet and runs" + Colors.RESET;
        }else if (Math.random() > noTroubleChance) {
            printMessage = "You couldn't find any trouble";
        } else {
            printMessage = "You want trouble, stranger! You got it!\n" + Colors.RED + "Oof! Umph! Ow!\n" + Colors.RESET;
            int goldDiff = (int) (Math.random() * 10) + 1;
            if (Math.random() > noTroubleChance) {
                printMessage += "Okay, stranger! You proved yer mettle. Here, take my gold.";
                printMessage += "\nYou won the brawl and receive " + Colors.YELLOW + goldDiff + " gold." + Colors.RESET;
                hunter.changeGold(goldDiff);
            } else {
                printMessage += "That'll teach you to go lookin' fer trouble in MY town! Now pay up!";
                printMessage += "\nYou lost the brawl and pay " + goldDiff + " gold.";
                hunter.changeGold(-goldDiff);
            }
        }
        System.out.println(printMessage);
        printMessage = "";
    }

    public String toString() {
        return "This nice little town is surrounded by " + terrain.getTerrainName() + ".";
    }

    /**
     * Determines the surrounding terrain for a town, and the item needed in order to cross that terrain.
     *
     * @return A Terrain object.
     */
    private Terrain getNewTerrain() {
        double rnd = Math.random();
        if (rnd < (1.0/6)) {
            return new Terrain(Colors.CYAN + "Mountains" + Colors.RESET, "Rope");
        } else if (rnd < (1.0/3)) {
            return new Terrain(Colors.CYAN + "Ocean" + Colors.RESET, "Boat");
        } else if (rnd < (1.0/2)) {
            return new Terrain(Colors.CYAN + "Plains" + Colors.RESET, "Horse");
        } else if (rnd < (2.0/3)) {
            return new Terrain(Colors.CYAN + "Desert" + Colors.RESET, "Water");
        } else if (rnd < (5.0/6)){
            jungle = true;
            return new Terrain(Colors.CYAN + "Jungle" + Colors.RESET, "Machete");
        } else{
            return new Terrain(Colors.CYAN + "Marsh" + Colors.RESET, "Boots");
        }
    }

    /**
     * Determines whether a used item has broken.
     *
     * @return true if the item broke.
     */
    private boolean checkItemBreak() {
        double rand = Math.random();
        return (rand < 0.5);
    }
}