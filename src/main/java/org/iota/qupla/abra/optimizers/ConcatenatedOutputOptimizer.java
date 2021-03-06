package org.iota.qupla.abra.optimizers;

import org.iota.qupla.abra.block.AbraBlockBranch;
import org.iota.qupla.abra.block.site.AbraSiteMerge;
import org.iota.qupla.abra.block.site.base.AbraBaseSite;
import org.iota.qupla.abra.optimizers.base.BaseOptimizer;
import org.iota.qupla.qupla.context.QuplaToAbraContext;

public class ConcatenatedOutputOptimizer extends BaseOptimizer
{
  public ConcatenatedOutputOptimizer(final QuplaToAbraContext context, final AbraBlockBranch branch)
  {
    super(context, branch);
  }

  @Override
  protected void processSite(final AbraSiteMerge site)
  {
    //TODO replace concatenation by moving concatenated sites from body to outputs
  }

  @Override
  public void run()
  {
    for (index = 0; index < branch.outputs.size(); index++)
    {
      final AbraBaseSite site = branch.outputs.get(index);
      processSite((AbraSiteMerge) site);
    }
  }
}
