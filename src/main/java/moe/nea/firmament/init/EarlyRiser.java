
package moe.nea.firmament.init;

public class EarlyRiser implements Runnable {
    @Override
    public void run() {
        new HandledScreenRiser().addTinkerers();
        new SectionBuilderRiser().addTinkerers();
        new CompoundTagMutationFinalizationDetectorInjector().addTinkerers();
//		TODO: new ItemColorsSodiumRiser().addTinkerers();
    }
}
