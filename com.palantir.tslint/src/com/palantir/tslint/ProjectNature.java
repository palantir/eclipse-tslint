
package com.palantir.tslint;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class ProjectNature implements IProjectNature {

    public static final String NATURE_ID = "com.palantir.tslint.tslintNature";

    private IProject project;

    @Override
    public void configure() throws CoreException {
        IProjectDescription description = this.project.getDescription();
        ICommand[] commands = description.getBuildSpec();

        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(Builder.BUILDER_ID)) {
                return;
            }
        }

        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        ICommand command = description.newCommand();
        command.setBuilderName(Builder.BUILDER_ID);
        newCommands[newCommands.length - 1] = command;
        description.setBuildSpec(newCommands);
        this.project.setDescription(description, null);
    }

    @Override
    public void deconfigure() throws CoreException {
        IProjectDescription description = getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; ++i) {
            if (commands[i].getBuilderName().equals(Builder.BUILDER_ID)) {
                ICommand[] newCommands = new ICommand[commands.length - 1];
                System.arraycopy(commands, 0, newCommands, 0, i);
                System.arraycopy(commands, i + 1, newCommands, i,
                    commands.length - i - 1);
                description.setBuildSpec(newCommands);
                this.project.setDescription(description, null);
                return;
            }
        }
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

}
