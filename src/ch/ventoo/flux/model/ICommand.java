package ch.ventoo.flux.model;

/**
 * Created by nano on 25/09/14.
 */
public interface ICommand {

    public IResponse visit(ICommandVisitor visitor);

}
