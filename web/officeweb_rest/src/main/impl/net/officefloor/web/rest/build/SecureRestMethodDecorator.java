package net.officefloor.web.rest.build;

/**
 * {@link RestMethodDecorator} to handle HTTPS.
 */
public class SecureRestMethodDecorator implements RestMethodDecorator<Void> {

    @Override
    public void decorateRestMethod(RestMethodDecoratorContext<Void> context) {

        // Attempt to determine if method is secure
        Boolean isSecure = context.getConfiguration("secure", Boolean.class);
        if (isSecure != null) {
            context.setSecure(isSecure);
            return; // flag secure
        }

        // Attempt to determine from parents
        RestPathContext path = context.getPath();
        while (path != null) {

            // Determine if secure
            isSecure = path.getConfiguration("secure", Boolean.class);
            if (isSecure != null) {
                context.setSecure(isSecure);
                return; // flag secure
            }

            // Attempt next parent
            path = path.getParentPath();
        }
    }

}
