
package moe.nea.firmament.init;

public class EarlyRiser implements Runnable {
    @Override
    public void run() {
        new ClientPlayerRiser().addTinkerers();
        new HandledScreenRiser().addTinkerers();
        new SectionBuilderRiser().addTinkerers();
//		TODO: new ItemColorsSodiumRiser().addTinkerers();
    }
}
