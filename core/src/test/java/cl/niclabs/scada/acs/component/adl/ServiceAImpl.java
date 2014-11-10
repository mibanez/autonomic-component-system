package cl.niclabs.scada.acs.component.adl;

public class ServiceAImpl implements ServiceA {

	@Override
	public int foo() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (int) (Math.random() * 10);
	}

}
