package cl.niclabs.scada.acs.component.adl.interfaces;


import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.objectweb.fractal.adl.error.ErrorTemplateValidator;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;

public enum ACSInterfaceError implements ErrorTemplate {

	MISSING_CONTROLLER_DESCRIPTION("The controller description (composite or primitive) MUST be declared.");

	public static final String GROUP_ID = InterfaceErrors.GROUP_ID;
	
    private final int id;
    private final String format;
   
	private ACSInterfaceError(final String format, final Object... args) {
        this.id = ordinal();
        this.format = format;

        assert ErrorTemplateValidator.validErrorTemplate(this, args);
    }
	
    public int getErrorId() {
        return id;
    }

    public String getGroupId() {
        return GROUP_ID;
    }

    public String getFormatedMessage(final Object... args) {
        return String.format(format, args);
    }

    public String getFormat() {
        return format;
    }

}
