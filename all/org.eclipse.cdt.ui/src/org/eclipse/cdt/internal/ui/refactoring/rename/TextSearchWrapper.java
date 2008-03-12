/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.formatter.scanner.SimpleScanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;



/**
 * Wraps the platform text search and uses a scanner to categorize the text-matches
 * by location (comments, string-literals, etc.).
 */
public class TextSearchWrapper {
    public final static int SCOPE_FILE = 1;
    public final static int SCOPE_WORKSPACE = 2;
    public final static int SCOPE_RELATED_PROJECTS = 3;
    public final static int SCOPE_SINGLE_PROJECT = 4;
    public final static int SCOPE_WORKING_SET = 5;

    private static class SearchScope extends TextSearchScope {
        public static SearchScope newSearchScope(IWorkingSet ws) {
            IAdaptable[] adaptables= ws.getElements();
            ArrayList<IResource> resources = new ArrayList<IResource>();
            for (int i = 0; i < adaptables.length; i++) {
                IAdaptable adaptable = adaptables[i];
                IResource r= (IResource) adaptable.getAdapter(IResource.class);
                if (r != null) {
                    resources.add(r);
                }
            }
            return newSearchScope(resources.toArray(new IResource[resources.size()]), false);
		}
        
		public static SearchScope newSearchScope(IResource[] resources, boolean copy) {
			return new SearchScope(resources, copy);
		}

        private IResource[] fRootResources;
        private ArrayList<Matcher> fFileMatcher= new ArrayList<Matcher>();

        private SearchScope(IResource[] resources, boolean copy) {
            fRootResources= copy ? (IResource[]) resources.clone() : resources;
        }

		@Override
		public IResource[] getRoots() {
            return fRootResources;
        }

        @Override
		public boolean contains(IResourceProxy proxy) {
            if (proxy.isDerived()) {
                return false;
            }
            if (proxy.getType() == IResource.FILE) {
                return containsFile(proxy.getName());
            }
            return true;
		}

		private boolean containsFile(String name) {
            for (Iterator<Matcher> iter = fFileMatcher.iterator(); iter.hasNext();) {
                Matcher matcher = iter.next();
                matcher.reset(name);
                if (matcher.matches()) {
                    return true;
                }
            }
            return false;
        }

        public void addFileNamePattern(String filePattern) {
            Pattern p= Pattern.compile(filePatternToRegex(filePattern));
            fFileMatcher.add(p.matcher("")); //$NON-NLS-1$
		}

        private String filePatternToRegex(String filePattern) {
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < filePattern.length(); i++) {
                char c = filePattern.charAt(i);
                switch(c) {
                case '\\':
                case '(':
                case ')':
                case '{':
                case '}':
                case '.':
                case '[':
                case ']':
                case '$':
                case '^':
                case '+':
                case '|':
                    result.append('\\');
                    result.append(c);
                    break;
                case '?':
                    result.append('.');
                    break;
                case '*':
                    result.append(".*"); //$NON-NLS-1$
                    break;
                default:
                    result.append(c);
                break;
                }
            }
            return result.toString();
        }
    }

    public TextSearchWrapper() {}
    
    private TextSearchScope createSearchScope(IFile file, int scope, 
            String workingSetName, String[] patterns) {
        switch (scope) {
        	case SCOPE_WORKSPACE:
        	    return defineSearchScope(file.getWorkspace().getRoot(), patterns);
        	case SCOPE_SINGLE_PROJECT:
        	    return defineSearchScope(file.getProject(), patterns);
        	case SCOPE_FILE:
        	    return defineSearchScope(file, patterns);
        	case SCOPE_WORKING_SET: {
        	    TextSearchScope result= defineWorkingSetAsSearchScope(workingSetName, patterns);
        	    if (result == null) {
        	        result= defineSearchScope(file.getWorkspace().getRoot(), patterns);
        	    }
        		return result;
        	}
        }
	    return defineRelatedProjectsAsSearchScope(file.getProject(), patterns);
    }
    
    private TextSearchScope defineRelatedProjectsAsSearchScope(IProject project, String[] patterns) {
        HashSet<IProject> projects= new HashSet<IProject>();
        LinkedList<IProject> workThrough= new LinkedList<IProject>();
        workThrough.add(project);
        while (!workThrough.isEmpty()) {
            IProject prj= workThrough.removeLast();
            if (projects.add(prj)) {
                try {
                    workThrough.addAll(Arrays.asList(prj.getReferencedProjects()));
                    workThrough.addAll(Arrays.asList(prj.getReferencingProjects()));
                } catch (CoreException e) {
                    // need to ignore
                }
            }
        }
        IResource[] resources= projects.toArray(new IResource[projects.size()]);
        return defineSearchScope(resources, patterns);
    }

    private TextSearchScope defineWorkingSetAsSearchScope(String wsName, String[] patterns) {
        if (wsName == null) {
            return null;
        }
		IWorkingSetManager wsManager= PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet ws= wsManager.getWorkingSet(wsName);
		if (ws == null) {
		    return null;
		}
		SearchScope result= SearchScope.newSearchScope(ws); 
		applyFilePatterns(result, patterns);
		return result;
    }

    private void applyFilePatterns(SearchScope scope, String[] patterns) {
        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];
            scope.addFileNamePattern(pattern);
        }
    }

    private TextSearchScope defineSearchScope(IResource resource, String[] patterns) {
    	SearchScope result= SearchScope.newSearchScope(new IResource[]{resource}, false); 
        applyFilePatterns(result, patterns);
        return result;
    }
    
    private TextSearchScope defineSearchScope(IResource[] resources, String[] patterns) {
    	SearchScope result= SearchScope.newSearchScope(resources, true);           
        applyFilePatterns(result, patterns);
        return result;
    }
    
    /**
     * @param monitor
     */
    public IStatus searchWord(int scope, IFile resource, String workingSet, String[] patterns,
            String word, IProgressMonitor monitor, final List<CRefactoringMatch> target) {
        int startPos= target.size();
        TextSearchEngine engine= TextSearchEngine.create();
        StringBuffer searchPattern= new StringBuffer(word.length()+ 8);
        searchPattern.append("\\b"); //$NON-NLS-1$
        searchPattern.append("\\Q"); //$NON-NLS-1$
        searchPattern.append(word);
        searchPattern.append("\\E"); //$NON-NLS-1$
        searchPattern.append("\\b"); //$NON-NLS-1$

        Pattern pattern= Pattern.compile(searchPattern.toString());
        
        TextSearchScope searchscope= createSearchScope(resource, scope, workingSet, patterns);
        TextSearchRequestor requestor= new TextSearchRequestor() {
            @Override
			public boolean acceptPatternMatch(TextSearchMatchAccess access) {
            	IFile file= access.getFile();
            	ICElement elem= CoreModel.getDefault().create(file);
            	if (elem instanceof ITranslationUnit) {
            		target.add(new CRefactoringMatch(file, 
            				access.getMatchOffset(), access.getMatchLength(), 0));
            	}
            	return true;
            }
        };
        IStatus result= engine.search(searchscope, requestor, pattern, 
        		new SubProgressMonitor(monitor, 95));
        categorizeMatches(target.subList(startPos, target.size()), 
                new SubProgressMonitor(monitor, 5));

        return result;
    }
    
    public void categorizeMatches(List<CRefactoringMatch> matches, IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("TextSearch.monitor.categorizeMatches"), matches.size()); //$NON-NLS-1$
        IFile file= null;
        ArrayList<int[]> locations= null;
        for (Iterator<CRefactoringMatch> iter = matches.iterator(); iter.hasNext();) {
            CRefactoringMatch match = iter.next();
            IFile tfile= match.getFile();
            if (file == null || !file.equals(tfile)) {
                file= tfile;
                locations= new ArrayList<int[]>(); 
                computeLocations(file, locations);                
            }
            match.setLocation(findLocation(match, locations));            
            monitor.worked(1);
        }
    }

    final static Comparator<int[]> COMPARE_FIRST_INTEGER= new Comparator<int[]>() {
        public int compare(int[] o1, int[] o2) {
            return (o1)[0]-(o2)[0];
        }
    };
    private int findLocation(CRefactoringMatch match, ArrayList<int[]> states) {
        int pos= Collections.binarySearch(states, new int[] {match.getOffset()}, COMPARE_FIRST_INTEGER);
        if (pos<0) {
            pos= -pos-2;
            if (pos < 0) {
                pos=0;
            }
        }
        int endOffset= match.getOffset() + match.getLength();
        int location= 0;
        while (pos<states.size()) {
            int[] info= states.get(pos);
            if (info[0] >= endOffset) {
                break;
            }
            location |= info[1];
            pos++;
        }
        return location;
    }

    private void computeLocations(IFile file, ArrayList<int[]> locations) {
        Reader reader;
        SimpleScanner scanner= new SimpleScanner();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(file.getContents(), file.getCharset()));
        } catch (CoreException e) {
            return;
        } catch (UnsupportedEncodingException e) {
            return;
        }
        try {
            scanner.initialize(reader, null);
            scanner.setReuseToken(true);
            Token token;
            int lastState= 0;
            while((token= scanner.nextToken()) != null) {
                int state= CRefactory.OPTION_IN_CODE;
                switch(token.getType()) {
                	case Token.tLINECOMMENT:
                    case Token.tBLOCKCOMMENT:
                        state= CRefactory.OPTION_IN_COMMENT;
                		break;
                    case Token.tSTRING:
                    case Token.tLSTRING:
                    case Token.tCHAR:
                        state= CRefactory.OPTION_IN_STRING_LITERAL;
                    	break;
                    case Token.tPREPROCESSOR:
                        state= CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE;
                        break;
                    case Token.tPREPROCESSOR_DEFINE:
                        state= CRefactory.OPTION_IN_MACRO_DEFINITION;
                        break;
                    case Token.tPREPROCESSOR_INCLUDE:
                        state= CRefactory.OPTION_IN_INCLUDE_DIRECTIVE;
                        break;
                }
                if (state != lastState) {
                    locations.add(new int[] {token.getOffset(), state});
                    lastState= state;
                }
            }
        }
        finally {
            try {
                reader.close();
            } catch (IOException e1) {
            }
        }
    }
}
